package org.peerbox.watchservice;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWatchService extends AbstractWatchService {

	private static final Logger logger = LoggerFactory.getLogger(FolderWatchService.class);

	private static final long CLEANUP_TASK_DELAY = 5000;

	private Thread fileEventProcessor;
	private WatchService watcher;
    private final Map<WatchKey, Path> watchKeyToPath;

    private Timer timer;

	public FolderWatchService() {
		super();
		this.watchKeyToPath = new HashMap<WatchKey, Path>();
	}

	protected void onStarted() throws IOException {
		watcher = FileSystems.getDefault().newWatchService();

		logger.info("Scanning folder: {} ...", getFolderToWatch());
		watchKeyToPath.clear();
		registerFoldersRecursive(getFolderToWatch());
		logger.info("Scanning done.");

		fileEventProcessor = new Thread(new FolderWatchEventProcessor());
		fileEventProcessor.setName("WatchServiceFileEventProcessor");
		fileEventProcessor.start();

		logger.info("Watch Service started.");
	}

	protected void onStopped() throws Exception {
		// event processor thread
		if (fileEventProcessor != null) {
			fileEventProcessor.interrupt();
			fileEventProcessor = null;
		}

		// cancel all watch keys and clear key map
		watchKeyToPath.entrySet().forEach(entry -> {
			entry.getKey().cancel();
			logger.trace("Canceled watchkey: '{}'", entry.getValue());
		});
		watchKeyToPath.clear();

		// Java watch service
		if (watcher != null) {
			watcher.close();
			watcher = null;
		}

		// cleanup task / timer
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		logger.info("Watch Service stopped.");
	}

	private synchronized void registerFolder(final Path folder) throws IOException {
		// FIXME: containsValue has bad performance in case of many folders.
		// maybe bidirectional (e.g. BiMap from Guava) map would be an option.
		if (!watchKeyToPath.containsValue(folder)) {
			logger.info("Register folder: {}", folder);
			WatchKey key = folder.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
			watchKeyToPath.put(key, folder);
		}
	}

	private synchronized void registerFoldersRecursive(final Path folder) throws IOException {
		// register recursively all folders and subfolders
		Files.walkFileTree(folder, new RegisterFolderVisitor());
	}

	private synchronized void unregisterFolder(WatchKey folderKey) {
		folderKey.cancel();
		Path folder = watchKeyToPath.remove(folderKey);
		logger.info("Unregister folder: {}", folder);
	}

	private synchronized void cleanupFolderRegistrations() {
		// find watch keys of deleted folders
		Set<WatchKey> keySet = new HashSet<>(watchKeyToPath.keySet());
		Set<WatchKey> keysToCancel = new HashSet<>();
		for (WatchKey key : keySet) {
			Path folder = watchKeyToPath.get(key);
			if (folder != null && !Files.exists(folder, NOFOLLOW_LINKS)) {
				keysToCancel.add(key);
			}
		}

		// unregister folders (cancel watch key)
		for (WatchKey key : keysToCancel) {
			unregisterFolder(key);
		}
	}

	private synchronized void scheduleCleanupTask() {
		// cancel previous task if existing
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		timer = new Timer(getClass().getName());
		timer.schedule(new CleanupFolderRegistrationsTask(), CLEANUP_TASK_DELAY);
	}

	@SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> castWatchEvent(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	private class FolderWatchEventProcessor implements Runnable {
		@Override
		public void run() {
			processEvents();
		}

		private void processEvents() {
			for (;;) {

				// wait for key to be signaled
				WatchKey key = null;
				try {
					key = watcher.take();
				} catch (InterruptedException iex) {
					logger.trace("Folder Watch Event Processor interrupted (stop watching folder).");
					return;
				}

				Path dir = watchKeyToPath.get(key);
				if (dir == null) {
					logger.error("WatchKey not recognized!!");
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {

					// FIXME: how to handle this event?
					// means Watcher lost some events due to too many events
					if (event.kind() == OVERFLOW) {
						logger.warn("OVERFLOW of WatchService - some eventy may be lost.");
						continue;
					}

					@SuppressWarnings("unchecked")
					Kind<Path> kind = (Kind<Path>) event.kind();

					// Context for directory entry event is the file name of entry
					WatchEvent<Path> ev = castWatchEvent(event);
					Path name = ev.context();
					Path child = dir.resolve(name);

					// if directory is created, and watching recursively, then
					// register it and its sub-directories
					if (kind == ENTRY_CREATE) {
						try {
							if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
								registerFoldersRecursive(child);
							}
						} catch (IOException ioex) {
							// registration of new folder failed.
							logger.warn("Could not register new folder: {}", child, ioex);
						}
					}

					// print out event
//					logger.debug("{}: {}", event.kind().name(), child);
					handleEvent(kind, child);

				}

				// reset key and remove from set if directory no longer accessible
				boolean valid = key.reset();
				if (!valid) {
					unregisterFolder(key);
					// all directories are inaccessible
					if (watchKeyToPath.isEmpty()) {
						logger.info("No more paths to watch, exit event processing loop.");
						break;
					}
				}

				// schedule a cleanup task that iterates over all directories and updates watch keys (adds or
				// removes them)
				scheduleCleanupTask();

				// watch service was stopped in the meantime
				if(!isRunning.get()) {
					return;
				}
			}
		}

		/**
		 * Precondition: Event and child must not be null.
		 * @param kind type of the event (create, modify, ...)
		 * @param source Identifies the related file.
		 */
		private void handleEvent(Kind<Path> kind, Path source) {
			try {
				if(PathUtils.isFileHidden(source)){
					return;
				}
				if (kind.equals(ENTRY_CREATE)) {
					addNotifyEvent(new NotifyFileCreated(source));
				} else if (kind.equals(ENTRY_MODIFY)) {
					addNotifyEvent(new NotifyFileModified(source));
				} else if (kind.equals(ENTRY_DELETE)) {
					addNotifyEvent(new NotifyFileDeleted(source));
				} else if (kind.equals(OVERFLOW)) {
					// error - overflow... should not happen here (continue if such an event occurs).
					// handled already
					logger.warn("Overflow event from watch service. Too many events?");
				} else {
					logger.warn("Unknown event received");
				}
			} catch (InterruptedException iex) {
				// put into queue failed
				logger.info("Handling event interrupted.", iex);
			}
		}
	}


	private class CleanupFolderRegistrationsTask extends TimerTask {
		@Override
		public void run() {
			try {
				logger.info("Running cleanup for registered folders.");
				registerFoldersRecursive(getFolderToWatch());
				cleanupFolderRegistrations();
			} catch (IOException e) {
				logger.warn("Could not register folders ({})", e.getMessage(), e);
			}
		}
	}

	private class RegisterFolderVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult preVisitDirectory(Path folder, BasicFileAttributes attrs) {
			try {
				registerFolder(folder);
			} catch (IOException ioex) {
				logger.warn("Could not register folder: {} ({})", folder, ioex.getMessage(), ioex);
			}
			return FileVisitResult.CONTINUE;
		}
	}
}

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWatchService extends AbstractWatchService {

	private static final Logger logger = LoggerFactory.getLogger(FolderWatchService.class);
	private static final long CLEANUP_TASK_DELAY = 5000;
	
	private Path folderToWatch;
	private Thread fileEventProcessor;
	private WatchService watcher;
    private Map<WatchKey, Path> watchKeyToPath;
    
    private Timer timer;
    
	public FolderWatchService(Path rootFolderToWatch) throws IOException {
		super();
		this.folderToWatch = rootFolderToWatch;
        this.watchKeyToPath = new HashMap<WatchKey, Path>();
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		watcher = FileSystems.getDefault().newWatchService();

		logger.info("Scanning {} ...", folderToWatch);
		watchKeyToPath.clear();
		registerFoldersRecursive(folderToWatch);
		logger.info("Scanning done.");
		
		fileEventProcessor = new Thread(new FolderWatchEventProcessor());
		fileEventProcessor.start();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		// event processor thread
		if(fileEventProcessor != null) {
			fileEventProcessor.interrupt();
			fileEventProcessor.join();
			fileEventProcessor = null;
		}
		// java watch service
		if(watcher != null) {
			watcher.close();
			watcher = null;
		}
		// cleanup task / timer
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		// key map
		watchKeyToPath.clear();
	}

	private synchronized void registerFolder(final Path folder) throws IOException {
		// FIXME: containsValue has bad performance in case of many folders. 
		// maybe bidirectional (e.g. from guava library) map would be a fix for that.
		if(!watchKeyToPath.containsValue(folder)) {
			logger.info("Register folder: {}", folder);
		    WatchKey key = folder.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
		    watchKeyToPath.put(key, folder);
		}
	}

	private synchronized void registerFoldersRecursive(final Path start) throws IOException {
	    // register recursively all folders and subfolders
	    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult preVisitDirectory(Path folder, BasicFileAttributes attrs)
	        {
	            try {
					registerFolder(folder);
				} catch (IOException e) {
					logger.warn("Could not register folder: {} ({})", folder, e.getMessage());
				}
	            return FileVisitResult.CONTINUE;
	        }
	    });
	}
	
	private synchronized void unregisterFolder(WatchKey folderKey) {
		folderKey.cancel();
		Path p = watchKeyToPath.remove(folderKey);
		logger.info("Unregister folder: {}", p);
	}
	
	private synchronized void cleanupFolderRegistrations() {
		// find watch keys of deleted folders
		Set<Entry<WatchKey, Path>> entrySet = new HashSet<Entry<WatchKey,Path>>(watchKeyToPath.entrySet());
		Set<WatchKey> keysToCancel = new HashSet<WatchKey>();
		for(Entry<WatchKey, Path> e : entrySet) {
			if(!Files.exists(e.getValue(), NOFOLLOW_LINKS)) {
				keysToCancel.add(e.getKey());
			}
		}
		
		// unregister folders (cancel watch key)
		for(WatchKey k : keysToCancel) {
			unregisterFolder(k);
		}
	}
	
	private synchronized void scheduleCleanupTask() {
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		
		timer = new Timer(getClass().getName());
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					logger.info("Running cleanup for registered folders.");
					registerFoldersRecursive(folderToWatch);
					cleanupFolderRegistrations();
				} catch (IOException e) {
					logger.warn("Could not register folders ({})", e.getMessage());
				}
			}
			
		}, CLEANUP_TASK_DELAY);
	}

	@SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> castWatchEvent(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

	private class FolderWatchEventProcessor implements Runnable {
		@Override
		public void run() {
			processEvents();
		}

		private void processEvents() {
			for (;;) {

				// wait for key to be signalled
				WatchKey key = null;
				try {
					key = watcher.take();
				} catch (InterruptedException iex) {
					logger.debug("Folder Watch Event Processor interrupted. Stop processing events.");
					return;
				}

				Path dir = watchKeyToPath.get(key);
				if (dir == null) {
					logger.error("WatchKey not recognized!!");
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {

					// FIXME: how to handle this event?
					if (event.kind() == OVERFLOW) {
						logger.warn("OVERFLOW");
						System.exit(1);
						continue;
					}

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
							// TODO: handle exception
							// ignore to keep sample readable
							logger.warn("Could not register new folder.", ioex);
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
				
				// schedule a cleanup task that iterates over all directories and updates watch keys (adds or removes them)
				scheduleCleanupTask();
			}
		}

		/**
		 * Precondition: Event and child must not be null.
		 * @param kind type of the event (create, modify, ...)
		 * @param filePath Identifies the related file.
		 */
		private void handleEvent(Kind<Path> kind, Path filePath) {
			try {
				if (kind.equals(ENTRY_CREATE)) {
					addNotifyEvent(new NotifyFileCreated(filePath));
				} else if (kind.equals(ENTRY_MODIFY)) {
					addNotifyEvent(new NotifyFileModified(filePath));
				} else if (kind.equals(ENTRY_DELETE)) {
					addNotifyEvent(new NotifyFileDeleted(filePath));
				} else if (kind.equals(OVERFLOW)) {
					// TODO: error - overflow... should not happen here (continue if such an event occurs)
					logger.warn("Overflow event from watch service. Too many events?");
				} else {
					logger.warn("Unknown event received");
				}
			} catch (InterruptedException iex) {
				logger.info("Handling event interrupted.", iex);
			}
		}
	}
}

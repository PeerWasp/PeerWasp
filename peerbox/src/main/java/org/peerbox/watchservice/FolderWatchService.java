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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWatchService {

	private static final Logger logger = LoggerFactory.getLogger(FolderWatchService.class);
	
	private Path rootFolder;
	private Thread eventProcessor;
	private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    
    private List<IFileEventListener> eventListeners;
    


	public FolderWatchService(Path rootFolderToWatch) throws IOException {
		this.rootFolder = rootFolderToWatch;
		this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.eventListeners = new ArrayList<IFileEventListener>();
	}
	
	public void start() throws Exception {
		
        logger.info("Scanning {} ...", rootFolder);
        registerFoldersRecursive(rootFolder);
        logger.info("Scanning done.");
 
        // enable trace after initial registration
        this.trace = true;
        
		eventProcessor = new Thread(new FolderWatchEventProcessor());
		eventProcessor.setDaemon(true); // keep running 
		eventProcessor.start();
	}

	public void stop() throws Exception {
		if(eventProcessor != null) {
			eventProcessor.interrupt();
			eventProcessor.join();
			// TODO: maybe reset all buffers, queues, maps, ... -> reset, event?
		}
		if(watcher != null) {
			watcher.close();
		}
	}

	public void addFileEventListener(IFileEventListener listener) {
		eventListeners.add(listener);
	}

	public void removeFileEventListener(IFileEventListener listener) {
		eventListeners.remove(listener);
	}

	public List<IFileEventListener> getFileEventListeners() {
		return eventListeners;
	}

	private void notifyFileCreated(Path path) {
		for(IFileEventListener l : eventListeners) {
			l.onFileCreated(path);
		}
	}
	
	private void notifyFileModified(Path path) {
		for(IFileEventListener l : eventListeners) {
			l.onFileModified(path);
		}
	}

	private void notifyFileDeleted(Path path) {
		for(IFileEventListener l : eventListeners) {
			l.onFileDeleted(path);
		}
	}
	
	private void registerFolder(Path folder) throws IOException {
	    WatchKey key = folder.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	    if (trace) {
	        Path prev = keys.get(key);
	        if (prev == null) {
	            logger.debug("register: {}\n", folder);
	        } else {
	            if (!folder.equals(prev)) {
	            	logger.debug("update: {} -> {}\n", prev, folder);
	            }
	        }
	    }
	    keys.put(key, folder);
	}

	private void registerFoldersRecursive(final Path start) throws IOException {
	    // register recursively all folders and subfolders
	    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult preVisitDirectory(Path folder, BasicFileAttributes attrs) throws IOException
	        {
	            registerFolder(folder);
	            return FileVisitResult.CONTINUE;
	        }
	    });
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
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					return;
				}

				Path dir = keys.get(key);
				if (dir == null) {
					System.err.println("WatchKey not recognized!!");
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {

					// TBD - provide example of how OVERFLOW event is handled
					if (event.kind() == OVERFLOW) {
						continue;
					}

					Kind<Path> kind = (Kind<Path>) event.kind();
					
					// Context for directory entry event is the file name of entry
					WatchEvent<Path> ev = castWatchEvent(event);
					Path name = ev.context();
					Path child = dir.resolve(name);

					// print out event
					logger.debug("{}: {}", event.kind().name(), child);
					handleEvent(kind, child);

					// if directory is created, and watching recursively, then
					// register it and its sub-directories
					if (kind == ENTRY_CREATE) {
						try {
							if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
								registerFoldersRecursive(child);
							}
						} catch (IOException x) {
							// TODO: handle exception
							// ignore to keep sample readbale
							logger.warn("Exception: {}", x.getMessage());
						}
					}
				}

				// reset key and remove from set if directory no longer accessible
				boolean valid = key.reset();
				if (!valid) {
					keys.remove(key);

					// all directories are inaccessible
					if (keys.isEmpty()) {
						break;
					}
				}
			}
		}

		/**
		 * Precondition: Event and child must not be null.
		 * @param kind 
		 * @param filePath Identifies the related file.
		 */
		private void handleEvent(Kind<Path> kind, Path filePath) {
			if(kind.equals(ENTRY_CREATE)) {
				notifyFileCreated(filePath);
			} else if(kind.equals(ENTRY_MODIFY)) {
				notifyFileModified(filePath);
			} else if(kind.equals(ENTRY_DELETE)){
				notifyFileDeleted(filePath);
			} else if(kind.equals(OVERFLOW)) {
				// TODO: error - overflow... should not happen here (continue if such an event occurs)
				logger.warn("Overflow event from watch service. Too many events?");
			} else {
				// TODO: unknown event
			}
		}
	}
}

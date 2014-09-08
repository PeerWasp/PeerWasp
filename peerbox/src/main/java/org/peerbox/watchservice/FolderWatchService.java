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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.hive2hive.core.api.interfaces.IFileObserver;
import org.hive2hive.core.api.interfaces.IFileObserverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWatchService implements IFileObserver {

	private static final Logger logger = LoggerFactory.getLogger(FolderWatchService.class);
	
	private Path rootFolder;
	private final WatchService watcher;
	private Thread eventProcessor;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    
    private Map<String, Action> filePathToAction;
    private Map<String, Set<String>> contentHashToFilePaths;
    
    private BlockingQueue<Action> actionQueue;
    
    private Thread actionExecutor;
	
	public Map<String, Action> getFilePathToAction() {
		return filePathToAction;
	}


	public BlockingQueue<Action> getActionQueue() {
		return actionQueue;
	}

	public FolderWatchService(Path rootFolderToWatch) throws IOException {
		this.rootFolder = rootFolderToWatch;
		this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        
        filePathToAction = new HashMap<String, Action>();
        contentHashToFilePaths = new HashMap<String, Set<String>>();

        actionQueue = new PriorityBlockingQueue<Action>(10, new FileActionTimeComparator());
        logger.info("Scanning {} ...", rootFolder);
        registerFoldersRecursive(rootFolder);
        logger.info("Scanning done.");
 
        // enable trace after initial registration
        this.trace = true;
	}
	
	@Override
	public void start() throws Exception {
		eventProcessor = new Thread(new FolderWatchEventProcessor());
		eventProcessor.setDaemon(false); // keep running 
		eventProcessor.start();
		
		actionExecutor = new Thread(new ActionExecutor(actionQueue, filePathToAction));
		actionExecutor.start();
	}

	@Override
	public void stop() throws Exception {
		if(eventProcessor != null) {
			eventProcessor.interrupt();
			// TODO: maybe reset all buffers, queues, maps, ... -> reset
		}
		
		if(actionExecutor != null) {
			actionExecutor.interrupt();
		}
	}

	@Override
	public void addFileObserverListener(IFileObserverListener listener) {
		
	}

	@Override
	public void removeFileObserverListener(IFileObserverListener listener) {
		
	}

	@Override
	public List<IFileObserverListener> getFileObserverListeners() {
		return null;
	}

	@Override
	public boolean isRunning() {
		return false;
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
    
    private void registerFolder(Path folder) throws IOException {
        WatchKey key = folder.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                logger.debug("register: %s\n", folder);
            } else {
                if (!folder.equals(prev)) {
                	logger.debug("update: %s -> %s\n", prev, folder);
                }
            }
        }
        keys.put(key, folder);
    }
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> castWatchEvent(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

	private class FolderWatchEventProcessor implements Runnable {
		@Override
		public void run() {
			//loadFileHashes();
			processEvents();
		}
		
		/*private void loadFileHashes() {
			String myDirectoryPath = "C:/Users/Claudio/Desktop/WatchServiceTest";

			// TODO Auto-generated method stub
			File dir = new File(myDirectoryPath);
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					// Do something with child
					filePathToAction.put(child.getPath().toString(), new Action());

				}
			}
		}*/

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
					logger.info("{}: {}", event.kind().name(), child);
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
			
			Kind<Path> eventKind = kind;
			Action lastAction = null;

			try {
				lastAction = getLastAction(eventKind, filePath);
				
				actionQueue.remove(lastAction);
				
				lastAction.setTimeStamp(System.currentTimeMillis());
				filePathToAction.put(filePath.toString(), lastAction);
				if(!contentHashToFilePaths.containsKey(lastAction.getContentHash())) {
					contentHashToFilePaths.put(lastAction.getContentHash(), new HashSet<String>());
				}
				contentHashToFilePaths.get(lastAction.getContentHash()).add(lastAction.getFilePath().toString());
				
				changeState(lastAction, eventKind);

				// add it with new timestamp / state
				actionQueue.add(lastAction);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("ActionQueue: " + actionQueue.size() + " Map: " + filePathToAction.size());
		}
		
		
	}
	
	/**
	 * @param eventKind Used to determine if an entry was created, deleted, or modified.
	 * @param filePath Identifies the related file.
	 * @return null if no FileContext related to the provided Path was found, the corresponding FileContext instance otherwise.
	 * @throws IOException
	 */
	private Action getLastAction(Kind<Path> eventKind, Path filePath) throws IOException{
		String keyForAction = null;
		
		keyForAction = filePath.toString();
		if(!filePathToAction.containsKey(keyForAction)) {
			filePathToAction.put(keyForAction, new Action(new InitialState(), filePath));
		}
		return filePathToAction.get(keyForAction);
	}
		
//		
//		
//		if (eventKind.equals(ENTRY_CREATE)) {
//			
//			//byte[] fileHashRaw = EncryptionUtil.generateMD5Hash(filePath.toString().getBytes());
//			
//			if (filePath != null) {
//				// File exists
//				
//			
//			}
//			
//		} else if (eventKind.equals(ENTRY_DELETE) || eventKind.equals(ENTRY_MODIFY)) {
//			keyForAction = filePath.toString();
//			lastAction = filePathToAction.get(keyForAction);
//			
//		} else {
//			System.out.println("Undefined event type!");
//		}
//		return lastAction;
//	}
	
	/**
	 * Performs the change according to the implemented state pattern
	 * @param action must not be null
	 * @param eventKind must not be null
	 */
	private void changeState(Action action, Kind<Path> eventKind){
		if(eventKind.equals(ENTRY_CREATE)){
			//standard event handling (e.g. move from INIT STATE to CREATE STATE
			boolean isMoveEvent = false;
			Action deleteAction = null;
			String contentHash = action.getContentHash();
			Set<String> filePaths = contentHashToFilePaths.get(contentHash);
			if(filePaths != null) {
				long minTimeDiff = Long.MAX_VALUE;
				for(String path : filePaths) {
					Action a = filePathToAction.get(path);
					if(a.getCurrentState() instanceof DeleteState) {
						long diff = action.getTimestamp() - a.getTimestamp();
						if(diff < minTimeDiff) {
							minTimeDiff = diff;
							deleteAction = a;
						}
					}
				}
				if(deleteAction != null) {
					isMoveEvent = true;
					contentHashToFilePaths.get(contentHash).remove(deleteAction.getFilePath().toString());
					filePathToAction.remove(deleteAction.getFilePath().toString());
				}
			}
			

			if(isMoveEvent) {
				action.handleMoveEvent(deleteAction.getFilePath());
			} else {
				action.handleCreateEvent();
			}
			
		} else if(eventKind.equals(ENTRY_DELETE)){
			action.handleDeleteEvent();
			filePathToAction.remove(action.getFilePath().toString());
			Set<String> filePaths = contentHashToFilePaths.get(action.getContentHash());
			filePaths.remove(action.getFilePath().toString());
			
		} else if(eventKind.equals(ENTRY_MODIFY)){
			String oldContentHash = action.getContentHash();
			action.handleModifyEvent();
			// update the map
			Set<String> filePaths = contentHashToFilePaths.get(oldContentHash);
			filePaths.remove(action.getFilePath().toString());
			if(!contentHashToFilePaths.containsKey(action.getContentHash())) {
				contentHashToFilePaths.put(action.getContentHash(), new HashSet<String>());
			}
			contentHashToFilePaths.get(action.getContentHash()).add(action.getFilePath().toString());
		}
	}
	
	private class FileActionTimeComparator implements Comparator<Action> {
		@Override
		public int compare(Action a, Action b) {
			return Long.compare(a.getTimestamp(), b.getTimestamp());
		}
	}
}

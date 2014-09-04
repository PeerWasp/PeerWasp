package org.peerbox.watchservice;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
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
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.hive2hive.core.api.interfaces.IFileObserver;
import org.hive2hive.core.api.interfaces.IFileObserverListener;
import org.hive2hive.core.security.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWatchService implements IFileObserver {

	private static final Logger logger = LoggerFactory.getLogger(FolderWatchService.class);
	
	private Path rootFolder;
	private final WatchService watcher;
	private Thread eventProcessor;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    
    private Map<String, Action> contenthashToAction;
    private Map<String, Action> filenameToAction;
    private BlockingQueue<Action> actionQueue;
    private BlockingQueue<Action> deleteQueue;
    
    
    private Thread actionExecutor;
	
	public Map<String, Action> getContenthashToAction() {
		return contenthashToAction;
	}

	public Map<String, Action> getFilenameToAction() {
		return filenameToAction;
	}

	public BlockingQueue<Action> getActionQueue() {
		return actionQueue;
	}

	public FolderWatchService(Path rootFolderToWatch) throws IOException {
		this.rootFolder = rootFolderToWatch;
		this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        
        contenthashToAction = new HashMap<String, Action>();
        filenameToAction = new HashMap<String, Action>();
        actionQueue = new PriorityBlockingQueue<Action>(10, new FileActionTimeComparator());
        deleteQueue = new PriorityBlockingQueue<Action>(10, new FileActionTimeComparator());
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
		
		actionExecutor = new Thread(new ActionExecutor(actionQueue, deleteQueue));
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
			loadFileHashes();
			processEvents();
		}
		
		private void loadFileHashes() {
			String myDirectoryPath = "C:/Users/Claudio/Desktop/WatchServiceTest";

			// TODO Auto-generated method stub
			File dir = new File(myDirectoryPath);
			  File[] directoryListing = dir.listFiles();
			  if (directoryListing != null) {
			    for (File child : directoryListing) {
			      // Do something with child
			    	try {
						contenthashToAction.put(Action.createStringFromByteArray(EncryptionUtil.generateMD5Hash(child)), new Action());
						filenameToAction.put(child.getPath().toString(), new Action());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			  } else {
				  
			  }
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
			Action lastContext = null;

			try {
				lastContext = getLastAction(eventKind, filePath);
				
				//no matches in the HashMaps, create a new FileContext with initial state
				if (lastContext == null) {
					String contentHash = "";
					if(filePath != null && filePath.toFile() != null){
						
						byte[] hash = EncryptionUtil.generateMD5Hash(filePath.toFile());
						if(hash != null){
							contentHash = Action.createStringFromByteArray(hash);//.toString();
						} else {
							contentHash = "";
						}
						
					} else {
						
					}
					lastContext = new Action(new InitialState(), filePath);
				
				// to update the queue, remove the found context...
				} else {
					actionQueue.remove(lastContext);
					//deleteQueue.remove(lastContext);
				}
				
				//and add it with new timestamp / state
				lastContext.setTimeStamp(Calendar.getInstance().getTimeInMillis());
				/*if(filePath != null && filePath.toFile() != null){
					lastContext.setContentHash(Action.createStringFromByteArray(EncryptionUtil.generateMD5Hash(filePath.toFile())));
				}*/
				//System.out.println(deleteQueue.size() + " " + actionQueue.size());
				changeState(lastContext, eventKind);
/*				if(lastContext.getCurrentState() instanceof DeleteState){
					
				} else if(lastContext.getCurrentState() instanceof CreateState){
					
					
				}*/
				//System.out.println(lastContext.getCurrentState().getClass().toString());
				actionQueue.add(lastContext);
				//System.out.println(deleteQueue.size() + " " + actionQueue.size());
				filenameToAction.put(filePath.toString(), lastContext);
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		Action lastContext = null;
		
		if (eventKind.equals(ENTRY_CREATE)) {
			
			//byte[] fileHashRaw = EncryptionUtil.generateMD5Hash(filePath.toString().getBytes());
			
			if (filePath != null) {
				// File exists
				keyForAction = filePath.toString();
				lastContext = contenthashToAction.get(keyForAction);
			}
			
		} else if (eventKind.equals(ENTRY_DELETE) || eventKind.equals(ENTRY_MODIFY)) {
			keyForAction = filePath.toString();
			lastContext = filenameToAction.get(keyForAction);
			
		} else {
			System.out.println("Undefined event type!");
		}
		return lastContext;
	}
	
	/**
	 * Performs the change according to the implemented state pattern
	 * @param action must not be null
	 * @param eventKind must not be null
	 */
	private void changeState(Action action, Kind<Path> eventKind){
		if(eventKind.equals(ENTRY_CREATE)){
			//standard event handling (e.g. move from INIT STATE to CREATE STATE
			action.handleCreateEvent();

			
			/* Sequentially check all pending deletes for content hash equality.
			 * On the first match, delete the entry from the pending deletes and from the actionQueue,
			 * as it is replaced with the new action later. Furthermore, set the new action's state to MOVE
			 */
			System.out.println("deleteQueue.size(): " + deleteQueue.size() );
			for(Iterator<Action> it = deleteQueue.iterator(); it.hasNext();){
				Action delete = it.next();
				if(delete.getContentHash().equals(action.getContentHash())){
					System.out.println("MOVE: From " + delete.getFilePath() + " to " + action.getFilePath());
					
					actionQueue.remove(delete);						
					action.setCurrentState(new MoveState());
					it.remove();
					
					break;
				}
			}
			
		} else if(eventKind.equals(ENTRY_DELETE)){
			action.handleDeleteEvent();
			
			deleteQueue.remove(action);
			try {
				deleteQueue.put(action);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(deleteQueue.size() + " " + actionQueue.size());
			
			//deleteQueue.add(action);
		} else if(eventKind.equals(ENTRY_MODIFY)){
			action.handleModifyEvent();
		}
	}
	
	private class FileActionTimeComparator implements Comparator<Action> {
		@Override
		public int compare(Action a, Action b) {
			return Long.compare(a.getTimestamp(), b.getTimestamp());
		}
		
	}
    
}

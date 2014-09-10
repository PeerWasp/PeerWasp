package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.peerbox.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class FileEventManager implements IFileEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);
	
    private BlockingQueue<Action> actionQueue;
    private Map<Path, Action> filePathToAction;
	private SetMultimap<String, Path> contentHashToFilePaths;

	private Thread actionExecutor;
    
    private FileManager fileManager;
    
    public FileEventManager() {
    	actionQueue = new PriorityBlockingQueue<Action>(10, new FileActionTimeComparator());
        filePathToAction = new HashMap<Path, Action>();
        contentHashToFilePaths = HashMultimap.create();
        
		actionExecutor = new Thread(new ActionExecutor(this));
		actionExecutor.start();
    }

	@Override
	public void onFileCreated(Path path) {
		logger.debug("onFileCreated: {}", path);
		
		// get existing/initial action and remove it from queue (if contained in it)
		Action lastAction = getOrCreateAction(path);
		actionQueue.remove(lastAction);
	
		// detect "move" event by looking at recent deletes
		Action deleteAction = findDeleteActionOfMoveEvent(lastAction);
		if(deleteAction == null) {
			// regular create event
			lastAction.handleCreateEvent();
		} else {
			actionQueue.remove(deleteAction);
			// found matching delete event -> move
			
			System.out.println("Delete time: " + deleteAction.getTimestamp() + " Create time: " + lastAction.getTimestamp());
			lastAction.handleMoveEvent(deleteAction.getFilePath());
			// update lookup indices - remove mappings
			filePathToAction.remove(deleteAction.getFilePath());
			Set<Path> filePaths = contentHashToFilePaths.get(deleteAction.getContentHash());
			filePaths.remove(deleteAction.getFilePath());
		}
		
		// add action to the queue again as timestamp was updated
		actionQueue.add(lastAction);
	}

	private Action findDeleteActionOfMoveEvent(Action createAction) {
		// search for a delete action with equal content hash.
		// if multiple candidates exist, take the one with the smallest time difference (i.e. the 
		// closest regarding the event times of delete/create)
		Action deleteAction = null;
		String contentHash = createAction.getContentHash();
		Set<Path> filePaths = contentHashToFilePaths.get(contentHash);
		long minTimeDiff = Long.MAX_VALUE;
		for(Path path : filePaths) {
			Action del = filePathToAction.get(path);
			if(del.getCurrentState() instanceof DeleteState) {
				long timeDiff = createAction.getTimestamp() - del.getTimestamp();
				if(timeDiff < minTimeDiff) {
					minTimeDiff = timeDiff;
					deleteAction = del;
				}
			}
		}
		return deleteAction;
	}

	@Override
	public void onFileDeleted(Path path) {
		logger.debug("onFileDeleted: {}", path);
		
		// get existing/initial action and remove it from queue (if contained in it)
		Action lastAction = getOrCreateAction(path);
		actionQueue.remove(lastAction);
		
		// handle the delete event
		lastAction.handleDeleteEvent();
		
		// no update of lookup indices - neither path nor content hash changed!
		
		// add action to the queue again as timestamp was updated
		actionQueue.add(lastAction);
		System.out.println("Last element: " + getOrCreateAction(path).getCurrentState().getClass());
	}

	@Override
	public void onFileModified(Path path) {
		logger.debug("onFileModified: {}", path);
		
		// get existing/initial action and remove it from queue (if contained in it)
		Action lastAction = getOrCreateAction(path);
		actionQueue.remove(lastAction);
		
		// handle the modified event for this action
		String oldContentHash = lastAction.getContentHash();
		lastAction.handleModifyEvent();
		
		// update the lookup indices
		// 1. remove old mappings
		Set<Path> oldFilePaths = contentHashToFilePaths.get(oldContentHash);
		oldFilePaths.remove(lastAction.getFilePath());
		// 2. add new references
		Set<Path> newFilePaths = contentHashToFilePaths.get(lastAction.getContentHash());
		newFilePaths.add(lastAction.getFilePath());
		
		// add action to the queue again as timestamp was updated
		actionQueue.add(lastAction);
	}
	
	public BlockingQueue<Action> getActionQueue() {
		return actionQueue;
	}
	
	/**
	 * @param eventKind Used to determine if an entry was created, deleted, or modified.
	 * @param filePath Identifies the related file.
	 * @return null if no FileContext related to the provided Path was found, the corresponding FileContext instance otherwise.
	 * @throws IOException
	 */
	private Action getOrCreateAction(Path filePath) {
		Action action = null;
		if(!filePathToAction.containsKey(filePath)) {
			action = new Action(filePath);
			// add new action to lookup indices
			filePathToAction.put(filePath, action);
			Set<Path> filePaths = contentHashToFilePaths.get(action.getContentHash());
			filePaths.add(action.getFilePath());
		}
		action = filePathToAction.get(filePath);
		System.out.println("Found action with state: " + action.getCurrentState());
		return action;
	}
	
	private class FileActionTimeComparator implements Comparator<Action> {
		@Override
		public int compare(Action a, Action b) {
			return Long.compare(a.getTimestamp(), b.getTimestamp());
		}
	}
	
	public FileManager getFileManager() {
		return fileManager;
	}
	
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
}

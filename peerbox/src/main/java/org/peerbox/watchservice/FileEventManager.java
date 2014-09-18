package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.peerbox.FileManager;
import org.peerbox.watchservice.states.LocalDeleteState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class FileEventManager implements IFileEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);
	
    private BlockingQueue<Action> actionQueue;
    private Map<Path, Action> filePathToAction;
    private FolderComposite fileTree;
    
	public Map<Path, Action> getFilePathToAction() {
		return filePathToAction;
	}
    
    private SetMultimap<String, FileComponent> deletedFileComponents = HashMultimap.create();
    
    public FolderComposite getFileTree(){
    	return fileTree;
    }

	private SetMultimap<String, Path> contentHashToFilePaths;

	private Thread actionExecutor;
    
    private FileManager fileManager;
    
    public FileEventManager(Path rootPath) {
    	actionQueue = new PriorityBlockingQueue<Action>(10, new FileActionTimeComparator());
    	fileTree = new FolderComposite(rootPath);
        contentHashToFilePaths = HashMultimap.create();
        
		actionExecutor = new Thread(new ActionExecutor(this));
		actionExecutor.start();
    }

	@Override
	public void onFileCreated(Path path, boolean useFileWalker) {
		logger.debug("onFileCreated: {}", path);
		
		FileComponent createdComponent = createFileComponent(path, useFileWalker);
		Action lastAction = createdComponent.getAction();
		actionQueue.remove(lastAction);
	
		// detect "move" event by looking at recent deletes
		FileComponent deletedComponent = findDeletedComponentOfMoveEvent(createdComponent);
		//Action deleteAction = findDeleteActionOfMoveEvent(lastAction);
		if(deletedComponent == null) {
			// regular create event
			createdComponent.getAction().handleLocalCreateEvent();
			//lastAction.handleLocalCreateEvent();
		} else {
			actionQueue.remove(deletedComponent.getAction());
			// found matching delete event -> move
			
			System.out.println("Delete time: " + deletedComponent.getAction().getTimestamp() + " Create time: " + lastAction.getTimestamp());
			lastAction.handleLocalMoveEvent(deletedComponent.getAction().getFilePath());
			// update lookup indices - remove mappings
		}
		
		// add action to the queue again as timestamp was updated
		actionQueue.add(lastAction);
	}
	
	private FileComponent findDeletedComponentOfMoveEvent(FileComponent createdComponent){
		FileComponent deletedComponent = null;
		String contentHash = createdComponent.getAction().getContentHash();
		Set<FileComponent> deletedComponents = deletedFileComponents.get(contentHash);
		long minTimeDiff = Long.MAX_VALUE;
		for(FileComponent candidate : deletedComponents) {
			long timeDiff = createdComponent.getAction().getTimestamp() - candidate.getAction().getTimestamp();
			if(timeDiff < minTimeDiff) {
				minTimeDiff = timeDiff;
				deletedComponent = candidate;
			}
		}
		return deletedComponent;
	}

	//TODO: remove children from actionQueue as well!
	@Override
	public void onFileDeleted(Path path) {
		logger.debug("onFileDeleted: {}", path);
		
		//Get the fileComponent and remove it from the action queue
		FileComponent deletedComponent = deleteFileComponent(path);
		if(deletedComponent == null){
			return;
		}
		actionQueue.remove(deletedComponent.getAction());
		
		// handle the delete event
		deletedComponent.getAction().handleLocalDeleteEvent();
		
		// add action to the queue again as timestamp was updated
		actionQueue.add(deletedComponent.getAction());
		
		deletedFileComponents.put(deletedComponent.getAction().getContentHash(), deletedComponent);
	}

	@Override
	public void onFileModified(Path path) {
		logger.debug("onFileModified: {}", path);
		
		//Get component to modify and remove it from action queue
		FileComponent toModify = getFileComponent(path);
		if(toModify == null){
			return;
		}
		
		Action lastAction = toModify.getAction();
		actionQueue.remove(toModify.getAction());
		
		//handle the modify-event
		//TODO remove hash update from handler!
		lastAction.handleLocalModifyEvent();
		
		//put the component (used to trigger hash updates in whole hierarchy)
		fileTree.putComponent(path.toString(), toModify);
		
		actionQueue.add(lastAction);
	}
	
	public BlockingQueue<Action> getActionQueue() {
		return actionQueue;
	}

	
	private FileComponent createFileComponent(Path filePath, boolean useFileWalker){
		FileComponent createdComponent;
		// create and add the correct component
		
		//if the created component is a directory, we can use the filewalker to add all children recursively
		if(filePath.toFile().isDirectory()){
			createdComponent = new FolderComposite(filePath);
			fileTree.putComponent(filePath.toString(), createdComponent);
			if(useFileWalker){
				FileWalker walker = new FileWalker(filePath, this);
				walker.indexDirectoryRecursively();
			}
		//simple file, just add it and return
		} else {
			createdComponent = new FileLeaf(filePath);
			fileTree.putComponent(filePath.toString(), createdComponent);
		}
		return createdComponent;
	}
	
	private FileComponent getFileComponent(Path filePath){
		return fileTree.getComponent(filePath.toString());
	}
	
	private FileComponent deleteFileComponent(Path filePath){
		return fileTree.deleteComponent(filePath.toString());
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
	
	public FileEventManager getThis(){
		return this;
	}
}

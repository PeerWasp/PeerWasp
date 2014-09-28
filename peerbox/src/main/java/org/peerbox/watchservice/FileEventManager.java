package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDownloadEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.peerbox.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class FileEventManager implements IFileEventListener, org.hive2hive.core.events.framework.interfaces.IFileEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);
	
	private Thread actionExecutor;
    private FileManager fileManager;
    
    private BlockingQueue<FileComponent> fileComponentQueue; 
    private FolderComposite fileTree;
    
    private SetMultimap<String, FileComponent> deletedByContentHash = HashMultimap.create();
    private Map<String, FolderComposite> deletedByContentNamesHash = new HashMap<String, FolderComposite>();
    
    private boolean maintainContentHashes = true;

    
    public FolderComposite getFileTree(){ //maybe synchronize this method?
    	return fileTree;
    }
    
    public FileEventManager(Path rootPath) {
    	fileComponentQueue = new PriorityBlockingQueue<FileComponent>(10, new FileActionTimeComparator());
    	fileTree = new FolderComposite(rootPath, true);

		actionExecutor = new Thread(new ActionExecutor(this));
		actionExecutor.start();
    }
    
    /**
     * @param rootPath is the root folder of the tree
     * @param maintainContentHashes set to true if content hashes have to be maintained. Content hash changes are
     * then propagated upwards to the parent directory.
     */
    public FileEventManager(Path rootPath, boolean maintainContentHashes) {
    	fileComponentQueue = new PriorityBlockingQueue<FileComponent>(10, new FileActionTimeComparator());
    	fileTree = new FolderComposite(rootPath, true);
        this.maintainContentHashes = maintainContentHashes;
		
        actionExecutor = new Thread(new ActionExecutor(this));
		actionExecutor.start();
    }
    
    public SetMultimap<String, FileComponent> getDeletedFileComponents(){
    	return this.deletedByContentHash;
    }
    
    /**
     * Handles incoming create events the following way:
     * If the created component is a folder, check if it corresponds to a
     * previous delete, and trigger an optimized move based on the folder's
     * structure. Otherwise, make a complete content discovery.
     * 
     * Furthermore, check if a move based on folder/file content is possible to trigger
     * a conventional move operation (this is expected in particular when ordinary files 
     * are moved and the optimized move operation is not possible), otherwise just handle 
     * the event as a conventional create
     */
	@Override
	public void onFileCreated(Path path, boolean useFileWalker) {
		logger.debug("onFileCreated: {}", path);
		
		FileComponent createdComponent = createFileComponent(path);
		getFileTree().putComponent(path.toString(), createdComponent);
		
		if(createdComponent instanceof FolderComposite){
			String moveCandidateHash = null;
			FolderComposite moveCandidate = null;
			moveCandidateHash = discoverSubtreeStructure(path);
			moveCandidate = deletedByContentNamesHash.get(moveCandidateHash);
			if(moveCandidate != null){
				System.out.println("Folder Move detected!");
				initiateOptimizedMove(moveCandidate, path);
				return;
			} else {
				createdComponent = discoverSubtreeCompletely(path);
				getFileTree().putComponent(createdComponent.getPath().toString(), createdComponent);
			}
		}
		fileComponentQueue.remove(createdComponent);
	
		// detect "move" event by looking at recent deletes
		FileComponent deletedComponent = findDeletedByContent(createdComponent);
		Action createAction = createdComponent.getAction();
		
		if(deletedComponent == null) {
			createAction.handleLocalCreateEvent();
		} else {
			Action deleteAction = deletedComponent.getAction();
			fileComponentQueue.remove(deletedComponent);
			createAction.handleLocalMoveEvent(deleteAction.getFilePath());
		}
		// add action to the queue again as timestamp was updated
		fileComponentQueue.add(createdComponent);
	}

	/**
	 * This function runs the FileWalker to discover the complete content of a subtree
	 * at the given location. The content hash of each file is computed, the content hash
	 * of a folder consists of a hash over contained files' content hashes. If these hashes 
	 * change, the change is propagated to the parent folder
	 * @param filePath represents the root of the subtree
	 * @return the complete subtree as a FolderComposite
	 */
	private FolderComposite discoverSubtreeCompletely(Path filePath) {
		FileWalker walker = new FileWalker(filePath, this);
		logger.debug("start complete subtree discovery at : {}", filePath);
		return walker.indexContentRecursively();
	}

	/**
	 * This function runs the FileWalker to discover the structure of the subtree
	 * at the given location. This means, content hashes are neither computed nor
	 * propagated upwards. The structure is represented using a hash on the names
	 * of the contained objects of each folder
	 * @param filePath represents the root of the subtree
	 * @return the hash representing the folder's structure
	 */
	private String discoverSubtreeStructure(Path filePath) {
		FileWalker walker = new FileWalker(filePath, this);
		logger.debug("start discovery of subtree structure at : {}", filePath);
		walker.indexNamesRecursively();
		return walker.getContentNamesHashOfWalkedFolder();
	}

	/**
	 * Searches the SetMultiMap<String, FileComponent> deletedByContentHash for
	 * a deleted FileComponent with the same content hash. If several exist, the temporally
	 * closest is returned.
	 * @param createdComponent The previously deleted component
	 * @return
	 */
	private FileComponent findDeletedByContent(FileComponent createdComponent){
		FileComponent deletedComponent = null;
		String contentHash = createdComponent.getContentHash();
		Set<FileComponent> deletedComponents = deletedByContentHash.get(contentHash);

		long minTimeDiff = Long.MAX_VALUE;
		for(FileComponent candidate : deletedComponents) {
			long timeDiff = createdComponent.getAction().getTimestamp() - candidate.getAction().getTimestamp();
			if(timeDiff < minTimeDiff) {
				minTimeDiff = timeDiff;
				deletedComponent = candidate;
			}
		}
		deletedComponents.remove(deletedComponent);
		return deletedComponent;
	}

	//TODO: remove children from actionQueue as well!
	/**
	 * Handles incoming delete events. The deleted component is added to
	 * a SetMultiMap<String, FileComponent>, the content hash is used as the key. Using
	 * this map, future create events can be mapped to previous deletes and indicate 
	 * a move operation. If the deleted component is a folder, the
	 * folder is additionally added to the deletedByContentNamesHash map with a hash 
	 * over the names of contained files as a key to allow optimized folder moves.
	 */
	@Override
	public void onFileDeleted(Path path) {
		logger.debug("onFileDeleted: {}", path);
		
		//Get the fileComponent and remove it from the action queue
		FileComponent deletedComponent = deleteFileComponent(path);
		if(deletedComponent == null){
			return;
		}
		fileComponentQueue.remove(deletedComponent);
		
		// handle the delete event
		deletedComponent.getAction().handleLocalDeleteEvent();
		
		//only add the file to the set of deleted files and to the action queue
		//if it was uploaded to the DHT before.
		if(deletedComponent.getIsUploaded()){
			deletedByContentHash.put(deletedComponent.getContentHash(), deletedComponent);
			if(deletedComponent instanceof FolderComposite){
				FolderComposite deletedComponentAsFolder = (FolderComposite)deletedComponent;
				deletedByContentNamesHash.put(deletedComponentAsFolder.getContentNamesHash(), deletedComponentAsFolder);
			}
			
			fileComponentQueue.add(deletedComponent);
		}
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
		fileComponentQueue.remove(toModify);
		
		//handle the modify-event
		lastAction.handleLocalModifyEvent();
		fileComponentQueue.add(toModify);
	}
	
	public BlockingQueue<FileComponent> getFileComponentQueue() {
		return fileComponentQueue;
	}

	
	private FileComponent createFileComponent(Path filePath){
		if(filePath.toFile().isDirectory()){
			return new FolderComposite(filePath, maintainContentHashes);
		} else {
			return new FileLeaf(filePath, maintainContentHashes);
		}
	}
	
	private FileComponent getFileComponent(Path filePath){
		return getFileTree().getComponent(filePath.toString());
	}
	
	private FileComponent deleteFileComponent(Path filePath){
		return getFileTree().deleteComponent(filePath.toString());
	}
	
	private class FileActionTimeComparator implements Comparator<FileComponent> {
		@Override
		public int compare(FileComponent a, FileComponent b) {
			return Long.compare(a.getAction().getTimestamp(), b.getAction().getTimestamp());
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

	public Map<String, FolderComposite> getDeletedByContentNamesHash() {
		return deletedByContentNamesHash;
	}

	/**
	 * @param moveCandidate represents the component, which is mostly
	 * likely the source of the move operation
	 * @param newPath is the new location to which the moved component is appended.
	 */
	public void initiateOptimizedMove(FolderComposite moveCandidate, Path newPath) {
		Path oldPath = moveCandidate.getPath();
		getFileTree().putComponent(newPath.toString(), moveCandidate);

		fileComponentQueue.remove(moveCandidate);
		moveCandidate.getAction().handleLocalMoveEvent(oldPath);
		fileComponentQueue.add(moveCandidate);
	}

	@Override @Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		logger.debug("onFileDelete: {}", fileEvent.getPath());
	}

	@Override @Handler
	public void onFileDownload(IFileDownloadEvent fileEvent) {
		logger.debug("onFileDownload: {}", fileEvent.getPath());
		
//		Path path = fileEvent.getPath();
//		FileComponent createdComponent = createFileComponent(path);
//		getFileTree().putComponent(path.toString(), createdComponent);
//		
//		createdComponent.getAction().handleRemoteCreateEvent();
		
	}

	@Override @Handler
	public void onFileMove(IFileMoveEvent fileEvent) {
		logger.debug("onFileMove: {}", fileEvent.getPath());
		// TODO Auto-generated method stub
		
	}
}

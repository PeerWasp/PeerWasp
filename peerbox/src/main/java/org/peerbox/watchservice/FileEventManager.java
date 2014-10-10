package org.peerbox.watchservice;

import java.nio.file.Files;
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
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.states.RemoteDeleteState;
import org.peerbox.watchservice.states.RemoteMoveState;
import org.peerbox.watchservice.states.RemoteUpdateState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class FileEventManager implements ILocalFileEventListener, org.hive2hive.core.events.framework.interfaces.IFileEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);
	
	private Thread actionExecutor;
    private FileManager fileManager;
    
    private BlockingQueue<FileComponent> fileComponentQueue; 
    private FolderComposite fileTree;
    
    private SetMultimap<String, FileComponent> deletedByContentHash = HashMultimap.create();
    private Map<String, FolderComposite> deletedByContentNamesHash = new HashMap<String, FolderComposite>();
    
    private boolean maintainContentHashes = true;

    
    public FileEventManager(Path rootPath) {
    	fileComponentQueue = new PriorityBlockingQueue<FileComponent>(1000, new FileActionTimeComparator());
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
	public void onLocalFileCreated(Path path, boolean useFileWalker) {
		logger.debug("onLocalFileCreate: {}", path);
		
		//FileComponent fileComponent = getOrCreateFileComponent(path);
		FileComponent fileComponent = getFileComponent(path);
		if(fileComponent == null){
			fileComponent = createFileComponent(path, Files.isRegularFile(path));
			getFileTree().putComponent(path.toString(), fileComponent);
		}
		logger.debug("File {} with content hash {} created", path, fileComponent.getContentHash());
		
		//if the create was a reaction to a remote move or update (i.e. H2H created the file), don't proceed
		if(fileComponent.getAction().getCurrentState() instanceof RemoteUpdateState ||
				fileComponent.getAction().getCurrentState() instanceof RemoteMoveState){
			fileComponentQueue.remove(fileComponent); //just in case the component is still in the queue
			fileComponent.getAction().getCurrentState().handleLocalCreateEvent();
			fileComponentQueue.add(fileComponent);
			return;
		}
		
		if(fileComponent instanceof FolderComposite){
			String moveCandidateHash = null;
			FolderComposite moveCandidate = null;
			moveCandidateHash = discoverSubtreeStructure(path);
			moveCandidate = deletedByContentNamesHash.get(moveCandidateHash);
			if(moveCandidate != null){
				System.out.println("Folder Move detected!");
				initiateOptimizedMove(moveCandidate, path);
				return;
			} else {
				fileComponent = discoverSubtreeCompletely(path);
				getFileTree().putComponent(fileComponent.getPath().toString(), fileComponent);
			}
		} else {
			fileComponent.bubbleContentHashUpdate();
		}
		
		fileComponentQueue.remove(fileComponent);
	
		// detect "move" event by looking at recent deletes
		FileComponent deletedComponent = findDeletedByContent(fileComponent);
		Action createAction = fileComponent.getAction();
		if(deletedComponent == null) {
			createAction.handleLocalCreateEvent();
		} else {
			Action deleteAction = deletedComponent.getAction();
			if(!fileComponentQueue.remove(deletedComponent)){
				System.err.println("Unexpected remove behaviour");
			}
			createAction.handleLocalMoveEvent(deleteAction.getFilePath());
		}
		// add action to the queue again as timestamp was updated
		fileComponentQueue.add(fileComponent);
	}

	@Override
	public void onLocalFileModified(Path path) {
		logger.debug("onFileModified: {}", path);
		
		//Get component to modify and remove it from action queue
		FileComponent toModify = getFileComponent(path);
		if(toModify == null){
			return;
		}
		
		if(toModify.isFolder()) {
			 // a folder can have only 1 version in H2H. Hence, we cannot execute an update!
			return;
		}
		Action lastAction = toModify.getAction();
		fileComponentQueue.remove(toModify);
		
		//handle the modify-event
		lastAction.handleLocalModifyEvent();
		
		updateChildrenTimestamps(toModify);
		fileComponentQueue.add(toModify);
	}

	private void updateChildrenTimestamps(FileComponent toModify) {
		if(toModify instanceof FolderComposite){
			FolderComposite toModifyAsFolderComposite = (FolderComposite)toModify;
			for(FileComponent child : toModifyAsFolderComposite.getChildren().values()){
				fileComponentQueue.remove(child);
				child.getAction().updateTimestamp();
				fileComponentQueue.add(child);
			}
		}
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
	public void onLocalFileDeleted(Path path) {
		logger.debug("onLocalFileDelete: {}", path);
		
		//Get the fileComponent and remove it from the action queue
//		FileComponent deletedComponent = 
//		if(deletedComponent == null){
//			return;
//		}
		
		FileComponent deletedComponent = deleteFileComponent(path);
		if(deletedComponent == null){
			logger.debug("File to delete not found{}", path);
			return;
		}
		
		fileComponentQueue.remove(deletedComponent);
		
		if(deletedComponent.getAction().getCurrentState() instanceof RemoteDeleteState){
			deletedComponent.getAction().handleLocalDeleteEvent();
			try {
				deletedComponent.getAction().execute(fileManager);
			} catch (NoSessionException | NoPeerConnectionException | IllegalFileLocation
					| InvalidProcessStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(deletedComponent.getAction().getCurrentState() instanceof RemoteUpdateState){
			deletedComponent.getAction().handleLocalDeleteEvent();
		} else {
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
		
	}

	@Override
	@Handler
	public void onFileDownload(IFileDownloadEvent fileEvent) {
		logger.debug("onFileDownload: {}", fileEvent.getPath());

		Path path = fileEvent.getPath();
		FileComponent fileComponent = getFileComponent(path);
		if(fileComponent == null) {
			fileComponent = createFileComponent(path, fileEvent.isFile());
		}
		getFileTree().putComponent(path.toString(), fileComponent);
		fileComponent.getAction().handleRemoteUpdateEvent();

	}

	@Override
	@Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		logger.debug("onFileDelete: {}", fileEvent.getPath());
		
		Path path = fileEvent.getPath();
		FileComponent fileComponent = getFileComponent(path);
		if(fileComponent != null) {
			fileComponent.getAction().handleRemoteDeleteEvent();
		}
	}

	@Override
	@Handler
	public void onFileMove(IFileMoveEvent fileEvent) {
		logger.debug("onFileMove: {}", fileEvent.getPath());
		
		// TODO Auto-generated method stub

		Path srcPath = fileEvent.getSrcPath();
		Path dstPath = fileEvent.getDstPath();
		
		FileComponent fileComponent = getFileComponent(srcPath);
		if(fileComponent == null){
			System.err.println("Error: Component to move does not exist, this should not happen");
			fileComponent = createFileComponent(srcPath, fileEvent.isFile());
			getFileTree().putComponent(srcPath.toString(), fileComponent);		
		}
		FileComponent deletedComponent = getFileTree().deleteComponent(srcPath.toString());
		fileComponentQueue.remove(deletedComponent);
		getFileTree().putComponent(dstPath.toString(), deletedComponent);
//		//switch to remoteMoveState()
		deletedComponent.getAction().getCurrentState().handleRemoteMoveEvent(srcPath);
		fileComponentQueue.add(deletedComponent);
	}

	private FileComponent getFileComponent(Path path){
		return getFileTree().getComponent(path.toString());
	}

	private FileComponent createFileComponent(Path path, boolean isFile) {
		FileComponent component = null;
		if (isFile) {
			component = new FileLeaf(path, maintainContentHashes);
		} else {
			component = new FolderComposite(path, maintainContentHashes);
		}
		return component;
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

	private FileComponent deleteFileComponent(Path filePath){
		return getFileTree().deleteComponent(filePath.toString());
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
		deletedByContentHash.remove(moveCandidate.getContentHash(), moveCandidate);
		deletedByContentNamesHash.remove(moveCandidate.getContentNamesHash());
		moveCandidate.getAction().handleLocalMoveEvent(oldPath);
		fileComponentQueue.add(moveCandidate);
	}

	public Map<String, FolderComposite> getDeletedByContentNamesHash() {
		return deletedByContentNamesHash;
	}

	public BlockingQueue<FileComponent> getFileComponentQueue() {
		return fileComponentQueue;
	}

	public SetMultimap<String, FileComponent> getDeletedFileComponents(){
		return this.deletedByContentHash;
	}

	public FolderComposite getFileTree(){ //maybe synchronize this method?
		return fileTree;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	private class FileActionTimeComparator implements Comparator<FileComponent> {
		@Override
		public int compare(FileComponent a, FileComponent b) {
			return Long.compare(a.getAction().getTimestamp(), b.getAction().getTimestamp());
		}
	}
}

package org.peerbox.watchservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.h2h.IFileRecoveryRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class FileEventManager implements IFileEventManager, ILocalFileEventListener, org.hive2hive.core.events.framework.interfaces.IFileEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);
	
	private Thread executorThread;

    private FileManager fileManager;
    
    private BlockingQueue<FileComponent> fileComponentQueue; 
    private FolderComposite fileTree;
    
    private SetMultimap<String, FileComponent> deletedByContentHash = HashMultimap.create();
    private Map<String, FolderComposite> deletedByContentNamesHash = new HashMap<String, FolderComposite>();
    private Map<String, FileLeaf> recoveredFileVersions = new HashMap<String, FileLeaf>();
    
    private boolean maintainContentHashes = true;
    private Path rootPath;

    
    public FileEventManager(Path rootPath, boolean waitForNotifications) {
    	fileComponentQueue = new PriorityBlockingQueue<FileComponent>(2000, new FileActionTimeComparator());
    	fileTree = new FolderComposite(rootPath, true, true);
    	this.rootPath = rootPath;
		executorThread = new Thread(new ActionExecutor(this, waitForNotifications));
		executorThread.start();
		
    }
    
    /**
     * @param rootPath is the root folder of the tree
     * @param maintainContentHashes set to true if content hashes have to be maintained. Content hash changes are
     * then propagated upwards to the parent directory.
     */
    public FileEventManager(Path rootPath, boolean waitForNotifications, boolean maintainContentHashes) {
    	fileComponentQueue = new PriorityBlockingQueue<FileComponent>(10, new FileActionTimeComparator());
    	fileTree = new FolderComposite(rootPath, true);
        this.maintainContentHashes = maintainContentHashes;
		this.rootPath = rootPath;
        executorThread = new Thread(new ActionExecutor(this, waitForNotifications));
		executorThread.start();
    }
    
    public Path getRootPath(){
    	return rootPath;
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
	 * 
	 * Assumptions:
	 * - The file exists
	 */
	@Override
	public void onLocalFileCreated(Path path) {
		logger.trace("onLocalFileCreated: {}", path);
		FileComponent file = getOrCreateFileComponent(path);

		if(path.toFile().isDirectory()){
			logger.debug("BEFORE Structure hash for {} is {} ", path, file.getStructureHash());
			String structureHash = discoverSubtreeStructure(path);
			logger.debug("AFTER Structure hash for {} is {} ", path, structureHash);
			file.setStructureHash(structureHash);
			
		}
		file.getAction().handleLocalCreateEvent();
		
		if(path.toFile().isDirectory()){
			discoverSubtreeCompletely(path);

		}
	}
	
	private FileComponent getOrCreateFileComponent(Path path){
		return getOrCreateFileComponent(path, null);
	}
	
	private FileComponent getOrCreateFileComponent(Path path, IFileEvent event) {
		FileComponent file = fileTree.getComponent(path.toString());
		if(file == null){
			logger.trace("FileComponent {} is new and now created.", path);
			if(event == null){
				file = createFileComponent(path, Files.isRegularFile(path));
			} else {
				file = createFileComponent(path, event.isFile());
			}

			file.getAction().setFile(file);
			file.getAction().setEventManager(this);
		}
		logger.debug("File {} has state {}", file.getPath(), file.getAction().getCurrentState().getClass());
		return file;
	}

//	public void onFileRecovered(Path path){
//		FileComponent fileComponent = getFileComponent(path);
//		if(fileComponent == null){
//			logger.trace("Recovered file component has to be created.");
//			fileComponent = createFileComponent(path, Files.isRegularFile(path));
//			getFileTree().putComponent(path.toString(), fileComponent);
//		} else {
//			logger.trace("Recovered file component exists.");
//		}
//		fileComponent.getAction().
//	}

	@Override
	public void onLocalFileModified(Path path) {
		logger.debug("onLocalFileModified: {}", path);
		
		FileComponent file = getOrCreateFileComponent(path);
		if(file.isFolder()){
			logger.debug("File {} is a folder. Update rejected.", path);
			return;
		}
		String newHash = PathUtils.computeFileContentHash(path);
		if(file.getContentHash().equals(newHash)){
			logger.debug("Content hash did not change for file {}. Update rejected.", path);
			return;
		}
		file.getAction().handleLocalUpdateEvent();
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
		FileComponent file = getOrCreateFileComponent(path);
		logger.debug("OnLocalFileDelete structure hash of {} is  {}", path, file.getStructureHash());
		file.getAction().handleLocalDeleteEvent();
	}
	
	@Override
	@Handler
	public void onFileAdd(IFileAddEvent fileEvent){
		logger.debug("onFileAdd: {}", fileEvent.getFile().getPath());
		
		Path path = fileEvent.getFile().toPath();
		FileComponent file = getOrCreateFileComponent(path, fileEvent);
		file.getAction().setFile(file);
		file.getAction().setEventManager(this);
		file.getAction().handleRemoteCreateEvent();
	}
	

	@Override
	@Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		logger.debug("onFileDelete: {}", fileEvent.getFile().getPath());
		
		Path path = fileEvent.getFile().toPath();
		FileComponent file = getOrCreateFileComponent(path, fileEvent);
		file.getAction().handleRemoteDeleteEvent();
	}

	@Override
	@Handler
	public void onFileUpdate(IFileUpdateEvent fileEvent) {
		Path path = fileEvent.getFile().toPath();
		logger.debug("onFileUpdate: {}", path);

		FileComponent file = getOrCreateFileComponent(path);
		file.getAction().handleRemoteUpdateEvent();
	}
	
	@Override
	@Handler
	public void onFileMove(IFileMoveEvent fileEvent) {
		logger.debug("onFileMove: {}", fileEvent.getFile().getPath());

		Path srcPath = fileEvent.getSrcFile().toPath();
		Path dstPath = fileEvent.getDstFile().toPath();
		logger.debug("Handle move from {} to {}", srcPath, dstPath);
		
		FileComponent source = getOrCreateFileComponent(srcPath);
		source.getAction().handleRemoteMoveEvent(dstPath);
	}
	
	public void onFileRecoveryRequest(IFileRecoveryRequestEvent fileEvent){
		File currentFile = fileEvent.getFile();
		if(currentFile == null || currentFile.isDirectory()){
			logger.error("Try to recover non-existing file or directory: {}", currentFile.getPath());
			return;
		}
		
		FileComponent file = fileTree.getComponent(currentFile.getPath());
		file.getAction().handleRecoverEvent(fileEvent.getVersionToRecover());
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
	public FolderComposite discoverSubtreeCompletely(Path filePath) {
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
	public String discoverSubtreeStructure(Path filePath) {
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
	public FileComponent findDeletedByContent(FileComponent createdComponent){
		FileComponent deletedComponent = null;
		String contentHash = createdComponent.getContentHash();
		logger.trace("Contenthash to search for: {}", contentHash);
		Set<FileComponent> deletedComponents = deletedByContentHash.get(contentHash);

		
		for(FileComponent comp : deletedComponents){
			logger.trace("Compoonent {} with hash {}", comp.getPath(), comp.getContentHash());
		}
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

	public FileComponent deleteFileComponent(Path filePath){
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

	public synchronized FolderComposite getFileTree(){ //maybe synchronize this method?
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

	@Override
	public void onFileShare(IFileShareEvent fileEvent) {
		// TODO Auto-generated method stub
		
	}


}

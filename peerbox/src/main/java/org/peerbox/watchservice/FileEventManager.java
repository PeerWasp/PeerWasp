package org.peerbox.watchservice;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.peerbox.h2h.IFileRecoveryRequestEvent;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FileEventManager implements IFileEventManager, ILocalFileEventListener, org.hive2hive.core.events.framework.interfaces.IFileEventListener {

	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);

    private final BlockingQueue<FileComponent> fileComponentQueue;
    private final FileTree fileTree;

    @Inject
	public FileEventManager(final FileTree fileTree) {
		fileComponentQueue = new PriorityBlockingQueue<FileComponent>(2000, new FileActionTimeComparator());
		this.fileTree = fileTree;
	}

//	@Inject
//	public FileEventManager(){
//		fileTree = null;
//		fileComponentQueue = null;
//	}

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
		logger.debug("onLocalFileCreated: {} Manager ID {}", path, hashCode());
		FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		file.setIsSynchronized(true);
		if(path.toFile().isDirectory()){
			String structureHash = fileTree.discoverSubtreeStructure(path, this);
			file.setStructureHash(structureHash);
		}
		file.getAction().handleLocalCreateEvent();

		if(path.toFile().isDirectory()){
			fileTree.discoverSubtreeCompletely(path, this);
		}
	}

	@Override
	public void onLocalFileModified(Path path) {
		logger.debug("onLocalFileModified: {}", path);

		FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		if(file.isFolder()){
			logger.debug("File {} is a folder. Update rejected.", path);
			return;
		}


		boolean hasChanged = file.bubbleContentHashUpdate();
		if (!hasChanged) {
			logger.debug("Content hash did not change for file {}. Update rejected.", path);
			return;
		}
		file.getAction().handleLocalUpdateEvent();
	}

	public void onFileRecoveryRequest(IFileRecoveryRequestEvent fileEvent){
		logger.trace("onFileRecoveryRequest: {}", fileEvent.getFile().getAbsolutePath());
		File currentFile = fileEvent.getFile();
		if(currentFile == null || currentFile.isDirectory()){
			logger.error("Try to recover non-existing file or directory: {}", currentFile.getPath());
			return;
		}

//		FileComponent file = fileTree.getComponent(currentFile.getPath());
		int version = fileEvent.getVersionToRecover();

		String recoveredFileName = PathUtils.getRecoveredFilePath(fileEvent.getFile().getName(), version).toString();
		Path pathOfRecoveredFile = Paths.get(currentFile.getParent()).resolve(Paths.get(recoveredFileName));
		FileComponent file = fileTree.getOrCreateFileComponent(pathOfRecoveredFile, this);
		fileTree.putFile(pathOfRecoveredFile, file);
		file.getAction().handleRecoverEvent(currentFile, fileEvent.getVersionToRecover());
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
		FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		logger.debug("OnLocalFileDelete structure hash of {} is  {}", path, file.getStructureHash());
		file.getAction().handleLocalDeleteEvent();
	}

	public void onFileDesynchronized(Path path){
		logger.debug("Desynchronize file {}", path);
		FileComponent file = fileTree.getOrCreateFileComponent(path, this);

		file.setIsSynchronized(false);
		fileTree.deleteFile(path);
		PathUtils.deleteRecursively(path.toFile());
	}

	public void onFileSynchronized(Path path, boolean isFolder){

		logger.debug("Synchronize file {}", path);
		FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		if(file.getIsSynchronized() == true){
			return;
		}
		fileTree.putFile(path, file);
		file.propagateIsUploaded();
//		FileComponent file = fileTree.addAndPutFile(path);
		file.setIsSynchronized(true);
		onFileAdd(new FileAddEvent(path.toFile(), isFolder));
	}

	@Override
	@Handler
	public void onFileAdd(IFileAddEvent fileEvent){
		logger.debug("onFileAdd: {}", fileEvent.getFile().getPath());

		Path path = fileEvent.getFile().toPath();
		FileComponent file = fileTree.getOrCreateFileComponent(path, fileEvent, this);
		if(!checkForSynchronizedAncestor(path)){
			logger.debug("The file {} is in folder that is not synchronized. Event ignored.", path);
			return;
		} else {
			logger.debug("The file {} is in folder that is synchronized: ", path);
			file.setIsSynchronized(true);
//			file.getAction().setFile(file);
			file.getAction().setEventManager(this);
			file.getAction().handleRemoteCreateEvent();
		}
		file.getAction().setFile(file);
		file.getAction().setEventManager(this);
		file.getAction().handleRemoteCreateEvent();
	}

	private boolean checkForSynchronizedAncestor(Path path){
		logger.debug("Path {}", path);
		FileComponent file = fileTree.getFile(path);
		if(file == null){
			logger.debug("Didnt find {}", path);
			return checkForSynchronizedAncestor(path.getParent());
		} else {
			logger.debug("Return : {}", file.getIsSynchronized());
			return file.getIsSynchronized();
		}
	}

	@Override
	@Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		logger.debug("onFileDelete: {}", fileEvent.getFile().getPath());

		Path path = fileEvent.getFile().toPath();
		FileComponent file = fileTree.getOrCreateFileComponent(path, fileEvent, this);
		file.getAction().handleRemoteDeleteEvent();
	}

	@Override
	@Handler
	public void onFileUpdate(IFileUpdateEvent fileEvent) {
		Path path = fileEvent.getFile().toPath();
		logger.debug("onFileUpdate: {}", path);

		FileComponent file = fileTree.getOrCreateFileComponent(path, this);

//		if(!checkForSynchronizedAncestor(path)){
//			return;
//		}

		file.getAction().handleRemoteUpdateEvent();
	}

	@Override
	@Handler
	public void onFileMove(IFileMoveEvent fileEvent) {
		logger.debug("onFileMove: {}", fileEvent.getFile().getPath());

		Path srcPath = fileEvent.getSrcFile().toPath();
		Path dstPath = fileEvent.getDstFile().toPath();
		logger.debug("Handle move from {} to {}", srcPath, dstPath);

		FileComponent source = fileTree.getOrCreateFileComponent(srcPath, this);

		source.getAction().handleRemoteMoveEvent(dstPath);
	}

	public void onLocalFileHardDelete(Path toDelete){
		logger.trace("onLocalFileHardDelete: {} Manager ID {}", toDelete, this.hashCode());

		FileComponent file = fileTree.getOrCreateFileComponent(toDelete, this);
		file.getAction().handleLocalHardDeleteEvent();
	}

	@Override
	public void onFileShare(IFileShareEvent fileEvent) {
		// TODO: download, notify user?
	}

	public BlockingQueue<FileComponent> getFileComponentQueue() {
		return fileComponentQueue;
	}

	public synchronized IFileTree getFileTree(){
		return fileTree;
	}

	private class FileActionTimeComparator implements Comparator<FileComponent> {
		@Override
		public int compare(FileComponent a, FileComponent b) {
			return Long.compare(a.getAction().getTimestamp(), b.getAction().getTimestamp());
		}
	}

}

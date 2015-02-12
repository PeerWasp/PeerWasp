package org.peerbox.watchservice;

import java.nio.file.Files;
import java.nio.file.Path;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.peerbox.app.manager.file.FileDesyncMessage;
import org.peerbox.app.manager.file.IFileMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FileEventManager implements IFileEventManager, ILocalFileEventListener, IFileEventListener {

	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);

	private final ActionQueue fileComponentQueue;
	private final FileTree fileTree;
	private final MessageBus messageBus;

    @Inject
	public FileEventManager(final FileTree fileTree, MessageBus messageBus) {
    	this.fileComponentQueue = new ActionQueue();
		this.fileTree = fileTree;
		this.messageBus = messageBus;
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
	public void onLocalFileCreated(final Path path) {
		logger.debug("onLocalFileCreated: {} - Manager ID {}", path, hashCode());

		final boolean isFolder = Files.isDirectory(path);
		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
//		if(file.isUploaded() && !file.isSynchronized() && file.getAction().getCurrentState() instanceof InitialState){
//			logger.trace("The file {} has already been uploaded and was recreated. Resolve conflict.");
//			ConflictHandler.resolveConflict(path, true);
//			return;
//		}
		file.setIsSynchronized(true);

		if (isFolder) {
			String structureHash = fileTree.discoverSubtreeStructure(path, this);
			file.setStructureHash(structureHash);
		}

		file.getAction().handleLocalCreateEvent();

		if (isFolder) {
			fileTree.discoverSubtreeCompletely(path, this);
		}

		fileTree.persistFile(file);
	}

	@Override
	public void onLocalFileModified(final Path path) {
		logger.debug("onLocalFileModified: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		if (file.isFolder()) {
			logger.debug("File {} is a folder. Update rejected.", path);
			return;
		}

		boolean hasChanged = file.updateContentHash();
		if (!hasChanged) {
			logger.debug("Content hash did not change for file {}. Update rejected.", path);
			return;
		}

		file.getAction().handleLocalUpdateEvent();

		fileTree.persistFile(file);
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
	public void onLocalFileDeleted(final Path path) {
		publishMessage(new FileDesyncMessage(path));
		logger.debug("onLocalFileDelete: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		if (file.isFolder()) {
			logger.debug("onLocalFileDelete: structure hash of {} is '{}'",
					path, file.getStructureHash());
		}

		file.getAction().handleLocalDeleteEvent();

		fileTree.persistFile(file);
	}

	@Override
	public void onLocalFileHardDelete(final Path path) {
		logger.debug("onLocalFileHardDelete: {} - Manager ID {}", path, hashCode());

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		file.getAction().handleLocalHardDeleteEvent();
	}

	@Override
	public void onFileDesynchronized(final Path path) {
		logger.debug("onFileDesynchronized: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		file.setIsSynchronized(false);
		// fileTree.deleteFile(path);
		FileUtils.deleteQuietly(path.toFile());

		fileTree.persistFile(file);
	}

	@Override
	public void onFileSynchronized(final Path path, boolean isFolder) {
		logger.debug("onFileSynchronized: {}", path);

		// TODO: need to specify whether it is a folder or not?
		// is this even required if we do onFileAdd later?
		final FileComponent file = fileTree.getOrCreateFileComponent(path, !isFolder, this);
		if (file.isSynchronized()) {
			return;
		}
		fileTree.putFile(path, file);
		// FileCompositeUtils.setIsUploadedWithAncestors(file, true);
		file.setIsSynchronized(true);
		onFileAdd(new FileAddEvent(path.toFile(), isFolder));

		fileTree.persistFile(file);
	}

	private boolean hasSynchronizedAncestor(final Path path) {
		// TODO: maybe stop when rootPath is reached...!
		FileComponent file = fileTree.getFile(path);
		if (file == null) {
			logger.trace("checkForSynchronizedAncestor: Did not find {}", path);
			return hasSynchronizedAncestor(path.getParent());
		} else {
			logger.trace("checkForSynchronizedAncestor: isSynchronized({})", file.isSynchronized());
			return file.isSynchronized();
		}
	}

	@Override
	@Handler
	public void onFileAdd(final IFileAddEvent fileEvent){
		final Path path = fileEvent.getFile().toPath();
		logger.debug("onFileAdd: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, fileEvent.isFile(), this);
		if (!hasSynchronizedAncestor(path)) {
			logger.debug("File {} is in folder that is not synchronized. Event ignored.", path);
			// TODO: set isSynchronized = false?
			return;
		} else {
			logger.debug("File {} is in folder that is synchronized.", path);
			file.setIsSynchronized(true);
		}

		file.getAction().setFile(file);
		file.getAction().setFileEventManager(this);
		file.getAction().handleRemoteCreateEvent();

		fileTree.persistFile(file);
	}

	@Override
	@Handler
	public void onFileDelete(final IFileDeleteEvent fileEvent) {
		final Path path = fileEvent.getFile().toPath();
		logger.debug("onFileDelete: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, fileEvent.isFile(), this);
		file.getAction().handleRemoteDeleteEvent();
	}

	@Override
	@Handler
	public void onFileUpdate(final IFileUpdateEvent fileEvent) {
		final Path path = fileEvent.getFile().toPath();
		logger.debug("onFileUpdate: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		file.getAction().handleRemoteUpdateEvent();

		fileTree.persistFile(file);
	}

	@Override
	@Handler
	public void onFileMove(final IFileMoveEvent fileEvent) {
		final Path srcPath = fileEvent.getSrcFile().toPath();
		final Path dstPath = fileEvent.getDstFile().toPath();
		logger.debug("onFileMove: {} -> {}", srcPath, dstPath);

		final FileComponent source = fileTree.getOrCreateFileComponent(srcPath, this);
		source.getAction().handleRemoteMoveEvent(dstPath);

		fileTree.persistFileAndDescendants(source);
	}

	@Override
	public void onFileShare(IFileShareEvent fileEvent) {
		// TODO: share not implemented
	}

	@Override
	public ActionQueue getFileComponentQueue() {
		return fileComponentQueue;
	}

	@Override
	public synchronized IFileTree getFileTree() {
		return fileTree;
	}

	public MessageBus getMessageBus() {
		return messageBus;
	}

	private void publishMessage(IFileMessage message) {
		if (messageBus != null) {
			messageBus.publish(message);
		} else {
			logger.warn("No message sent, as message bus is null!");
		}
	}

}

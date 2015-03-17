package org.peerbox.watchservice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.IFileMessage;
import org.peerbox.app.manager.file.messages.FileExecutionStartedMessage;
import org.peerbox.app.manager.file.messages.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.messages.LocalShareFolderMessage;
import org.peerbox.app.manager.file.messages.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.messages.RemoteShareFolderMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.forcesync.ForceSyncCompleteMessage;
import org.peerbox.forcesync.ForceSyncMessage;
import org.peerbox.forcesync.IForceSyncHandler;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.states.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * The FileEventManager forms the glue between the events delivered by the
 * {@link org.peerbox.watchservice.FolderWatchService FolderWatchService} and
 * the PeerWasp core, in which the state for each file is maintained. To fulfill
 * this purpose, the FileEventManager provides a set of event handlers, which are
 * used by the {@link org.peerbox.watchservice.FolderWatchService FolderWatchService}
 * or other code parts (like the GUI) to forward the events to an {@link org.peerbox.
 * watchservice.Action Action} object coupled to a file. Depending on the type of
 * the event, additional measures may be taken into consideration, like applying events
 * recursively in case the triggering object is a folder.
 *
 * @author Claudio
 */
@Singleton
public class FileEventManager implements IFileEventManager, ILocalFileEventListener, IFileEventListener {

	private static final Logger logger = LoggerFactory.getLogger(FileEventManager.class);

	/**
	 * This queue contains FileComponents on which local or remote events happened that require
	 * some kind of network operation. The objects can be picked from the queu when no new events
	 * occured for a specified time. Check {@link org.peerbox.watchservice.ActionQueue}
	 */
	private final ActionQueue fileComponentQueue;

	/**
	 * Represents the file system view from the perspective
	 * of PeerWasp, which is influenced by local and remote file events.
	 */
	private final FileTree fileTree;

	/** Used to publish important events system-wide.*/
	private final MessageBus messageBus;

	/**
	 * If the execution of an {@link org.peerbox.watchservice.Action Action}
	 * definitely fails (i.e. repeatedly until the maximal number of attempts to
	 * re-execute is reached), the path is added to this set. As soon as the PeerWasp
	 * retries to execute it or clean it up, the file is removed again. This set is
	 * important to correctly represent failed operations in the {@link org.peerbox.
	 * presenter.settings.synchronization.Synchronization Synchronzation}.
	 */
	private final Set<Path> failedOperations;

//	private final Set<Path> sharedFolders;

	private boolean cleanupRunning;

	private Set<Path> pendingEvents = new ConcurrentHashSet<Path>();

	private Provider<IForceSyncHandler> forceSyncHandlerProvider;
	private IForceSyncHandler forceSyncHandler;
	/**
	 *
	 * @param fileTree The file tree representation of PeerWasp
	 * @param messageBus To publish events system-wide
	 */
    @Inject
	public FileEventManager(final FileTree fileTree, MessageBus messageBus) {
    	this.fileComponentQueue = new ActionQueue();
		this.fileTree = fileTree;
		this.messageBus = messageBus;
		this.failedOperations = new ConcurrentHashSet<Path>();
	}
    
    @Inject
    public void setForceSyncHandlerProvider(Provider<IForceSyncHandler> forceSyncHandlerProvider){
    	this.forceSyncHandlerProvider = forceSyncHandlerProvider;
    	forceSyncHandlerProvider.get();
    }
    
    public IForceSyncHandler getForceSyncHandler(){
    	if(forceSyncHandler == null){
    		forceSyncHandler = forceSyncHandlerProvider.get();
    	}
    	return forceSyncHandler;
    }

    /**
	 * Handles incoming create events. First of all, it gets or creates the
	 * corresponding {@link org.peerbox.watchservice.filetree.composite.FileComponent
	 * FileComponent} from the {@link #fileTree} and markes it as synchronized.
	 *
	 * If the created component is a folder, check if the operation is part of a
	 * move operation by checking the folder's
	 * structure hash. Otherwise, make a complete content discovery.
	 *
	 * If it is a file, check if a move based on file content is possible to trigger
	 * a conventional move operation, otherwise just handle
	 * the event as a conventional create.
	 *
	 * Assumptions:
	 * - The file exists, hence this handler is not invoked manually if the file has been
	 * deleted before.
	 */
	@Override
	public void onLocalFileCreated(final Path path) {
		if(cleanupRunning){
			pendingEvents.add(path);
			return;
		}

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
		file.updateContentHash();

		file.getAction().handleLocalCreateEvent();

		//check if it is a folder and the move detection did not work:
		if (isFolder && fileTree.getFile(path).getAction().getCurrentState().getStateType() != StateType.INITIAL) {
			logger.trace("No move detected after folder {} was created. Initiate complete discovery.", path);
			fileTree.discoverSubtreeCompletely(path, this);
		}
	}

	/**
	 * Used to handdle local update events. The event is ignored if at least one of the
	 * following requirements is met: The object does not exist on disk, the object is
	 * a folder, or the objects content hash did not change. Otherwise, the event is forwarded
	 * to the core.
	 */
	@Override
	public void onLocalFileModified(final Path path) {
		if(cleanupRunning){
			pendingEvents.add(path);
			return;
		}

		logger.debug("onLocalFileModified: {}", path);


		if(!Files.exists(path) || Files.isDirectory(path)){
			logger.trace("File {} does not exist on disk or is a folder, discard local update", path);
			return;
		}
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
	}

	//TODO: remove children from actionQueue as well!
	/**
	 * Forwards the local delete event to the core. Additionally, it publishes a {@link
	 * org.peerbox.app.manager.file.messages.LocalFileDesyncMessage LocalFileDesyncMessage} using
	 * the {@link #messageBus} to inform GUI components.
	 */
	@Override
	public void onLocalFileDeleted(final Path path) {
		if(cleanupRunning){
			pendingEvents.add(path);
			return;
		}

		logger.debug("onLocalFileDelete: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		if (file.isFolder()) {
			logger.debug("onLocalFileDelete: structure hash of {} is '{}'",
					path, file.getStructureHash());
		}
		publishMessage(new LocalFileDesyncMessage(new FileInfo(file)));
		file.getAction().handleLocalDeleteEvent();

	}

	/**
	 * Triggered by the user using the Windows Explorer context menu option "PeerWasp->Delete" or
	 * the "Delete from network" option in the context menu of the view "Settings->Synchronization".
	 * Completely deletes a file from the network such that it is not recoverable anymore.
	 */
	@Override
	public void onLocalFileHardDelete(final Path path) {
		if(cleanupRunning){
			return;
		}

		logger.debug("onLocalFileHardDelete: {} - Manager ID {}", path, hashCode());

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		file.getAction().handleLocalHardDeleteEvent();
	}

	/**
	 * Triggered by the user using the view "Settings->Synchronization". By unchecking checkboxes,
	 * items can be soft-deleted, which is done by this event handler. This handler deletes the
	 * corresponding file or folder recursively.
	 */
	@Override
	public void onFileDesynchronized(final Path path) {
		if (cleanupRunning) {
			return;
		}

		logger.debug("onFileDesynchronized: {}", path);
		final FileComponent file = fileTree.getFile(path);
		if (file != null) {
			file.setIsSynchronized(false); //TODO: rec
			FileUtils.deleteQuietly(path.toFile());
		} else {
			logger.error("onFileDesynchronized: Did not find file component: {}", path);
		}
	}

	/**
	 * Triggered by the user using the view "Settings->Synchronization". By checking checkboxes,
	 * soft-deleted items can be restored by downloading them again.
	 */
	@Override
	public void onFileSynchronized(final Path path, boolean isFolder) {
		if(cleanupRunning){
			return;
		}

		logger.debug("onFileSynchronized: {}", path);

		// TODO: need to specify whether it is a folder or not?
		// is this even required if we do onFileAdd later?
		final FileComponent file = fileTree.getOrCreateFileComponent(path, !isFolder, this);
//		if (file.isSynchronized()) {
//			logger.trace("File {} is still synchronized, return!", path);
//			return;
//		}
		fileTree.putFile(path, file);
		// FileCompositeUtils.setIsUploadedWithAncestors(file, true);
		file.setIsSynchronized(true);
		onFileAdd(new FileAddEvent(path.toFile(), isFolder));

	}

	private boolean hasSynchronizedAncestor(final Path path) {
		// TODO: maybe stop when rootPath is reached...!
		FileComponent file = fileTree.getFile(path);
		if (file == null) {
//			logger.trace("checkForSynchronizedAncestor: Did not find {}", path);
			return hasSynchronizedAncestor(path.getParent());
		} else {
//			logger.trace("checkForSynchronizedAncestor: {} isSynchronized({})", path, file.isSynchronized());
//			return hasSynchronizedAncestor(path.getParent());
			return file.isSynchronized();
		}
	}

	/**
	 * This handler is for remote create events and is called by the network when
	 * new files are recognized. The file is only downloaded if it has an ancestor
	 * in the {@link #fileTree} that is existing and synchronized. Otherwise, the
	 * event is ignored.
	 */
	@Override
	@Handler
	public synchronized void onFileAdd(final IFileAddEvent fileEvent){
		if(cleanupRunning){
			pendingEvents.add(fileEvent.getFile().toPath());
			return;
		}

		final Path path = fileEvent.getFile().toPath();
		logger.debug("onFileAdd: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, fileEvent.isFile(), this);

		file.getAction().setFile(file);
		file.getAction().setFileEventManager(this);

		logger.trace("file {} has ID {}", path, file.hashCode());
		if (!hasSynchronizedAncestor(path)) {
			logger.debug("File {} is in folder that is not synchronized. Event ignored.", path);
			// TODO: set isSynchronized = false?
			file.setIsSynchronized(false);
			getMessageBus().publish(new FileExecutionStartedMessage(new FileInfo(file), StateType.INITIAL));
			//return;
		} else {
			logger.debug("File {} is in folder that is synchronized.", path);
			file.setIsSynchronized(true);
			file.getAction().handleRemoteCreateEvent();
		}




	}

	/**
	 * This handler is for remote delete events and is called by the network when
	 * a file has been definitely deleted. Besides forwarding the event to the core,
	 * this method publishes a {@link org.peerbox.app.manager.file.messages.RemoteFileDeletedMessage
	 * RemoteFileDeletedMessage} to notify the GUI.
	 */
	@Override
	@Handler
	public void onFileDelete(final IFileDeleteEvent fileEvent) {
		if(cleanupRunning){
			pendingEvents.add(fileEvent.getFile().toPath());
			return;
		}

		final Path path = fileEvent.getFile().toPath();
		logger.debug("onFileDelete: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, fileEvent.isFile(), this);
		file.getAction().handleRemoteDeleteEvent();

		FileInfo fileHelper = new FileInfo(file);
		messageBus.publish(new RemoteFileDeletedMessage(fileHelper));
	}

	/**
	 * This handler is for remote update events and is called by the network when
	 * a file has been changed remotely. This method only forwards the event to the core.
	 */
	@Override
	@Handler
	public void onFileUpdate(final IFileUpdateEvent fileEvent) {
		if(cleanupRunning){
			pendingEvents.add(fileEvent.getFile().toPath());
			return;
		}
		final Path path = fileEvent.getFile().toPath();
		logger.debug("onFileUpdate: {}", path);

		final FileComponent file = fileTree.getOrCreateFileComponent(path, this);
		file.getAction().handleRemoteUpdateEvent();
	}

	/**
	 * This handler is for remote move events and is called by the network when
	 * a file has been moved remotely. This method forwards the event to the core and
	 * publishes a {@link org.peerbox.app.manager.file.messages.RemoteFileMovedMessage
	 * RemoteFileMovedMessage} to inform the GUI.
	 */
	@Override
	@Handler
	public void onFileMove(final IFileMoveEvent fileEvent) {
		if(cleanupRunning){
			pendingEvents.add(fileEvent.getSrcFile().toPath());
			pendingEvents.add(fileEvent.getDstFile().toPath());
			return;
		}
		final Path srcPath = fileEvent.getSrcFile().toPath();
		final Path dstPath = fileEvent.getDstFile().toPath();
		logger.debug("onFileMove: {} -> {}", srcPath, dstPath);

		final FileComponent source = fileTree.getOrCreateFileComponent(srcPath, this);
		source.getAction().handleRemoteMoveEvent(dstPath);

		FileInfo srcFile = new FileInfo(srcPath, fileEvent.isFolder());
		FileInfo dstFile = new FileInfo(dstPath, fileEvent.isFolder());
		messageBus.publish(new RemoteFileMovedMessage(srcFile, dstFile));
	}

	/**
	 * Sharing is not supported in the first version of PeerWasp.
	 */
	@Override
	@Handler
	public void onFileShare(IFileShareEvent fileEvent) {
		String permissionStr = "";
		for (UserPermission p : fileEvent.getUserPermissions()) {
			permissionStr = permissionStr.concat(p.getUserId() + " ");
			if(p.getPermission() == PermissionType.READ){
				permissionStr = permissionStr.concat("Read");
			} else {
				permissionStr = permissionStr.concat("Read / Write");
			}
		}
		logger.info("Share: Invited by: {}, Permission: [{}]", fileEvent.getInvitedBy(), permissionStr, fileEvent.getFile());
		fileEvent.getInvitedBy();
		Set<UserPermission> permissions = fileEvent.getUserPermissions();
		String invitedBy = fileEvent.getInvitedBy();
		FileInfo file = new FileInfo(fileEvent);

		StringBuilder sb = new StringBuilder();
		sb.append("User ").append(invitedBy).append(" shared the folder ").
		append(fileEvent.getFile().toPath()).append(" with you.");
		getMessageBus().post(new InformationNotification("Shared folder", sb.toString())).now();
		publishMessage(new RemoteShareFolderMessage(file, permissions, invitedBy));
	}

	/**
	 * @return The {@link #fileComponentQueue}.
	 */
	@Override
	public ActionQueue getFileComponentQueue() {
		return fileComponentQueue;
	}

	/**
	 * @return The {@link #fileTree}.
	 */
	@Override
	public synchronized IFileTree getFileTree() {
		return fileTree;
	}

	/**
	 * @return The {@link #failedOperations} containing the {@link java.nio.file.Path
	 * Path}s of all failed Actions.
	 */
	@Override
    public Set<Path> getFailedOperations(){
    	return failedOperations;
    }

	/**
	 * @return The {@link #messageBus} used to publish events system-wide.
	 */
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

	@Handler
	public void onForceSync(ForceSyncMessage message){
		logger.trace("onForceSync: Block events and clear fileComponentQueue {}", message.getTopLevel());
		setCleanupRunning(true);
		fileComponentQueue.clear();
	}

	@Handler
	public void onForceSyncComplete(ForceSyncCompleteMessage message){
		logger.trace("Forced synchronization: Event block removed.");
		setCleanupRunning(false);
	}

	public void setCleanupRunning(boolean b) {
		cleanupRunning = b;
	}

	public Set<Path> getPendingEvents() {
		return pendingEvents;
	}
	
	
	public void initiateForceSync(Path topLevel){
		logger.trace("PeerWasp initiated a force synchronization automatically on {}", topLevel);
		IForceSyncHandler handler = getForceSyncHandler();
		if(handler != null){
			handler.forceSync( topLevel);
		}
		getMessageBus().publish(new InformationNotification("Forced synchronization", "Try to restore consistency"));
	}

}

package org.peerbox.presenter.settings.synchronization;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.util.Callback;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.stage.Window;
import javafx.scene.control.TreeCell;
import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.file.messages.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.messages.FileExecutionStartedMessage;
import org.peerbox.app.manager.file.messages.FileExecutionSucceededMessage;
import org.peerbox.app.manager.file.messages.LocalFileSoftDeleteMessage;
import org.peerbox.app.manager.file.messages.LocalShareFolderMessage;
import org.peerbox.app.manager.file.messages.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.messages.RemoteShareFolderMessage;
import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.forcesync.IForceSyncHandler;
import org.peerbox.share.IShareFolderHandler;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.states.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * This is the presenter class controlling the view "Settings > Synchronization".
 * @author Claudio
 *
 */
public class Synchronization implements Initializable, IExecutionMessageListener{

	private static final Logger logger = LoggerFactory.getLogger(Synchronization.class);

	@FXML private TreeView<PathItem> fileTreeView;
	@FXML private Button okButton;
	@FXML private Button cancelButton;
	@FXML private Button selectAllButton;
	@FXML private Button unselectAllButton;
	@FXML private Button refreshButton;

	private IFileEventManager eventManager;
	private IFileManager fileManager;

	/**
	 * These variables are used to build sets containing the paths of files
	 * and folder whose synchronization state has to be changed in the {@link
	 * org.peerbox.watchservice.FileEventManager FileEventManager}
	 */
	private TreeSet<FileInfo> toSynchronize = new TreeSet<FileInfo>(new FileInfoComparator());
	private TreeSet<FileInfo> toDesynchronize = new TreeSet<FileInfo>(new FileInfoComparator());

	private UserConfig userConfig;

	/**
	 * These variables are build directly from the {@link org.peerbox.
	 * watchservice.FileEventManager FileEventManager} to correctly
	 * populate the TreeView
	 */
//	private Set<Path> synchronizedFiles;
	private Set<Path> failedFiles = new HashSet<Path>();
	private Set<Path> executingFiles = new HashSet<Path>();

	private final Provider<IShareFolderHandler> shareFolderHandlerProvider;

	private final Provider<IFileRecoveryHandler> recoverFileHandlerProvider;

	private final Provider<IForceSyncHandler> forceSyncHandlerProvider;


	@Inject
	public Synchronization(IFileManager fileManager, FileEventManager eventManager,
			UserConfig userConfig, Provider<IFileRecoveryHandler> recoverFileHandlerProvider,
			Provider<IShareFolderHandler> shareFolderHandlerProvider,
			Provider<IForceSyncHandler> forceSyncHandlerProvider) {
		this.eventManager = eventManager;
		this.fileManager = fileManager;
		this.userConfig = userConfig;
		this.recoverFileHandlerProvider = recoverFileHandlerProvider;
		this.shareFolderHandlerProvider = shareFolderHandlerProvider;
		this.forceSyncHandlerProvider = forceSyncHandlerProvider;
	}

	public Set<FileInfo> getToSynchronize(){
		return toSynchronize;
	}

	public Set<FileInfo> getToDesynchronize(){
		return toDesynchronize;
	}

	public IFileEventManager getFileEventManager(){
		return eventManager;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("Initialize Synchronization!");
//		synchronizedFiles = getFileEventManager().getFileTree().getSynchronizedPathsAsSet();
		createTreeViewFromNetwork();
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Accept" button.
	 * @param event that was fired.
	 */
	@FXML
	public void acceptSyncAction(ActionEvent event) {
//		synchronizedFiles = eventManager.getFileTree().getSynchronizedPathsAsSet();

		for(FileInfo node : toSynchronize){
			if(!Files.exists(node.getPath()))
				eventManager.onFileSynchronized(node.getPath(), node.isFolder());
		}

		for(FileInfo node: toDesynchronize.descendingSet()){
			eventManager.onFileSoftDeleted(node.getPath());
		}

		toSynchronize.clear();
		toDesynchronize.clear();

		if(event.getTarget() != null && event.getTarget() instanceof Button){
			Button okButton = (Button)event.getTarget();
			Window window = okButton.getScene().getWindow();
			window.hide();
		}
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Select All" button.
	 * @param event that was fired.
	 */
	@FXML
	public void selectAllAction(ActionEvent event) {
		SyncTreeItem root = (SyncTreeItem)fileTreeView.getRoot();
		root.setSelected(true);
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Unselect all" button.
	 * @param event that was fired.
	 */
	@FXML
	public void unselectAllAction(ActionEvent event) {
		SyncTreeItem root = (SyncTreeItem)fileTreeView.getRoot();
		root.setSelected(true); // surprisingly, only setting to false has no effect!
		root.setSelected(false);
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Cancel" button.
	 * @param event that was fired.
	 */
	@FXML
	public void cancelAction(ActionEvent event) {
		if(event.getTarget() != null && event.getTarget() instanceof Button){
			Button cancelButton = (Button)event.getTarget();
			Window window = cancelButton.getScene().getWindow();
			window.hide();
		}
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Refresh" button.
	 * @param event that was fired.
	 */
	@FXML
	public void refreshAction(ActionEvent event){
//		synchronizedFiles = getFileEventManager().getFileTree().getSynchronizedPathsAsSet();
		failedFiles = getFileEventManager().getFailedOperations();
		createTreeViewFromNetwork();
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.manager.file.messages.FileExecutionStartedMessage
	 * FileExecutionStartedMessage} is published using the {@link org.peerbox.
	 * events.MessageBus MessageBus}. This method changes the corresponding
	 * {@link javafx.scene.control.CheckBoxTreeItem CheckBoxTreeItem} in the
	 * {@link javafx.scene.control.TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onExecutionStarts(FileExecutionStartedMessage message) {
		logger.trace("onExecutionStarts: {}", message.getFile().getPath());
		SyncTreeItem item = getOrCreateItem(message.getFile(), false);
		item.setProgressState(ProgressState.IN_PROGRESS);
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.manager.file.messages.RemoteFileDeletedMessage RemoteFileDeletedMessage} is
	 * published using the {@link org.peerbox.events.MessageBus MessageBus}.
	 * This method changes the corresponding {@link javafx.scene.control.
	 * CheckBoxTreeItem CheckBoxTreeItem} in the {@link javafx.scene.control.
	 * TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onFileRemotelyDeleted(RemoteFileDeletedMessage message){
		logger.trace("onFileRemotelyDeleted: {}", message.getFile().getPath());
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	removeTreeItem(message.getFile().getPath());
	        }
		});
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.manager.file.messages.RemoteFileMovedMessage RemoteFileMovedMessage} is
	 * published using the {@link org.peerbox.events.MessageBus MessageBus}.
	 * This method changes the corresponding {@link javafx.scene.control.
	 * CheckBoxTreeItem CheckBoxTreeItem} in the {@link javafx.scene.control.
	 * TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onFileRemotelyMoved(RemoteFileMovedMessage message){
		Path srcFile = message.getSourceFile().getPath();
		Path dstFile = message.getDestinationFile().getPath();
		logger.trace("onFileRemotelyMoved: {} --> {}", srcFile, dstFile);

		removeTreeItemInUIThread(srcFile);

		SyncTreeItem item = getOrCreateItem(message.getFile(), true);
		item.setProgressState(ProgressState.SUCCESSFUL);

//		updateIsSelectedInUIThread(item, message.getFile(), true);
		item.updateIsSelectedInUIThread(true);
	}


	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.manager.file.messages.FileExecutionSucceededMessage
	 * FileExecutionSucceededMessage} is published using the {@link org.peerbox.
	 * events.MessageBus MessageBus}. This method changes the corresponding
	 * {@link javafx.scene.control. CheckBoxTreeItem CheckBoxTreeItem} in the
	 * {@link javafx.scene.control.TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onExecutionSucceeds(FileExecutionSucceededMessage message) {
		logger.trace("Synchronization: onExecutionSucceeds: {}", message.getFile().getPath());
		StateType stateType = message.getStateType();
		switch(stateType){
		case LOCAL_HARD_DELETE:
			removeTreeItemInUIThread(message.getFile().getPath());
			break;
		case INITIAL:
			SyncTreeItem item = getTreeItem(message.getFile().getPath());
			if(item != null){
				item.setProgressState(ProgressState.SUCCESSFUL);
			}
			break;
		case LOCAL_MOVE:
			Path srcFile = message.getSourceFile().getPath();
//			Path dstFile = message.getFile().getPath();
			removeTreeItemInUIThread(srcFile);

			item = getOrCreateItem(message.getFile(), true);
			item.setProgressState(ProgressState.SUCCESSFUL);
			item.updateIsSelectedInUIThread(true);
//			updateIsSelectedInUIThread(item, message.getFile(), true);
			break;
		default:
			item = getOrCreateItem(message.getFile(), true);
			item.setProgressState(ProgressState.SUCCESSFUL);
//			updateIsSelectedInUIThread(item, message.getFile(), true);
			item.updateIsSelectedInUIThread(true);
		}
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.manager.file.messages.FileExecutionFailedMessage
	 * FileExecutionFailedMessage} is published using the {@link org.peerbox.
	 * events.MessageBus MessageBus}. This method changes the corresponding
	 * {@link javafx.scene.control. CheckBoxTreeItem CheckBoxTreeItem} in the
	 * {@link javafx.scene.control.TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onExecutionFails(FileExecutionFailedMessage message) {
		logger.trace("onExecutionFails: {}", message.getFile().getPath());

		SyncTreeItem item = getOrCreateItem(message.getFile(), false);
		item.setProgressState(ProgressState.FAILED);
		final SyncTreeItem item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	item2.setSelected(true);
	        }
		});
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.manager.file.messages.LocalFileSoftDeleteMessage LocalFileDesyncMessage} is published
	 * using the {@link org.peerbox.events.MessageBus MessageBus}. This method
	 * changes the corresponding {@link javafx.scene.control. CheckBoxTreeItem
	 * CheckBoxTreeItem} in the {@link javafx.scene.control.TreeView TreeView}
	 * accordingly.
	 */
	@Override
	@Handler
	public void onFileSoftDeleted(LocalFileSoftDeleteMessage message) {
		logger.trace("onFileSoftDeleted: {}", message.getFile().getPath());
		SyncTreeItem item = getTreeItem(message.getFile().getPath());

		item.setProgressState(ProgressState.DEFAULT);
		final SyncTreeItem item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	item2.setSelected(false);
	        }
	   });
	}

	@Override
	@Handler
	public void onRemoteFolderShared(RemoteShareFolderMessage message) {
		logger.trace("onRemoteFolderShared: {}", message.getFile().getPath());

		SyncTreeItem item = getOrCreateItem(message.getFile(), false);
		item.getValue().getPermissions().clear();
		item.getValue().getPermissions().addAll(message.getUserPermissions());
		item.setIsShared(true);
	}

	@Override
	@Handler
	public void onLocalFolderShared(LocalShareFolderMessage message) {
		logger.trace("onLocalFolderShared: {}", message.getFile().getPath());
		SyncTreeItem item = getOrCreateItem(message.getFile(), false);


		item.getValue().getPermissions().add(message.getInvitedUserPermission());
		item.setIsShared(true);
	}

	private void removeTreeItemInUIThread(Path srcFile) {
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	removeTreeItem(srcFile);
	        }
		});
	}

	private SyncTreeItem getOrCreateItem(FileInfo file, boolean isSynched) {
		SyncTreeItem item = getTreeItem(file.getPath());

		if(item != null){
			logger.trace("item != null for {}, change icon!", file.getPath());
		} else {
			item = createItem(file.getPath(), true, file.isFile());
			putTreeItem(item);
			logger.trace("item == null for {}", file.getPath());
		}
		return item;
	}

	private SyncTreeItem getTreeItem(Path path){
		SyncTreeItem root = (SyncTreeItem)fileTreeView.getRoot();
		return findTreeItem(root, path, false);
	}

	private void putTreeItem(SyncTreeItem item){
		SyncTreeItem root = (SyncTreeItem)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		Path pathLeft = prefix.relativize(item.getValue().getPath());
		putTreeItem(root, item, pathLeft);
		return;
	}

	private SyncTreeItem removeTreeItem(Path path){
		SyncTreeItem root = (SyncTreeItem)fileTreeView.getRoot();
		return findTreeItem(root, path, true);
	}

	private SyncTreeItem findTreeItem(SyncTreeItem item, Path path, boolean remove){
		Path wholePath = path;
		Path prefix = item.getValue().getPath();
		if(path.startsWith(prefix)){
			path = prefix.relativize(wholePath);
		}
		if(path.equals(Paths.get(""))){
			if(remove){
				item.getParent().getChildren().remove(item);
				item.unbind();
			}
			return item;
		}
		for(TreeItem<PathItem> child : item.getChildren()){
			SyncTreeItem castedChild = (SyncTreeItem)child;
			Path childNextLevel = prefix.relativize(child.getValue().getPath()).getName(0);
			if(childNextLevel.equals(path.getName(0))){
				 return findTreeItem(castedChild, wholePath, remove);
			 }
		}
		return null;
	}

	private void putTreeItem(SyncTreeItem parent, SyncTreeItem toPut, Path pathLeft){
		Path parentPath = parent.getValue().getPath();
		Path wholePath = parentPath.resolve(pathLeft);

		if(pathLeft.getNameCount() == 1){
			toPut.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler(this));
			Iterator<TreeItem<PathItem>> iter = parent.getChildren().iterator();
			while(iter.hasNext()){
				if(iter.next().getValue().getPath().equals(wholePath)){
					iter.remove();
				}
			}
			parent.getChildren().add(toPut);
			if(!toPut.isSelected() || !toPut.isIndeterminate()){
				parent.updateIsSelectedInUIThread(true);
			}
			toPut.bindTo(parent);
			return;
		} else {
			Iterator<TreeItem<PathItem>> iter = parent.getChildren().iterator();
			Path nextLevel = pathLeft.getName(0);
			Path pathToSearch = parentPath.resolve(nextLevel);
			while(iter.hasNext()){
				SyncTreeItem child = (SyncTreeItem)iter.next();
				if(child.getValue().getPath().equals(pathToSearch)){
					putTreeItem(child, toPut, child.getValue().getPath().relativize(wholePath));
					return;
				}
			}
			PathItem pathItem = new PathItem(pathToSearch, toPut.isSelected(), null);
			SyncTreeItem created = new SyncTreeItem(pathItem);
			toPut.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler(this));
			parent.getChildren().add(toPut);
			toPut.bindTo(parent);
			putTreeItem(created, toPut, toPut.getValue().getPath().relativize(wholePath));
		}
	}

	private void createTreeViewFromNetwork() {
		try {
			FileNode filesFromNetwork = fileManager.listFiles().execute();
			if(filesFromNetwork != null){
				createTreeView(filesFromNetwork);
			} else {
				logger.trace("Files from network are null");
			}
		} catch (NoSessionException | NoPeerConnectionException
				| InvalidProcessStateException | ProcessExecutionException e) {
			e.printStackTrace();
		}
	}

	private void createTreeView(FileNode fileNode){
		PathItem pathItem = new PathItem(userConfig.getRootPath(), false, fileNode.getUserPermissions());
		SyncTreeItem invisibleRoot = new SyncTreeItem(pathItem);
//		invisibleRoot.setIndependent(true);
	    fileTreeView.setRoot(invisibleRoot);
        fileTreeView.setEditable(false);

        fileTreeView.setCellFactory(new Callback<TreeView<PathItem>, TreeCell<PathItem>>(){
            @Override
            public TreeCell<PathItem> call(TreeView<PathItem> p) {
                return new CustomizedTreeCell(getFileEventManager(),
                		recoverFileHandlerProvider,
                		shareFolderHandlerProvider,
                		forceSyncHandlerProvider);
            }
        });

        fileTreeView.setShowRoot(false);


        addChildrensToTreeView(fileNode);
	}

	private void addChildrensToTreeView(FileNode fileNode){
		if(fileNode.getChildren() != null){

			List<FileNode> sortedChildren = fileNode.getChildren().stream().
					sorted((f1, f2) -> f1.getFile().compareTo(f2.getFile())).
					collect(Collectors.<FileNode>toList());

			for(FileNode topLevelNode : sortedChildren){
	        	Path path = topLevelNode.getFile().toPath();
	        	boolean isSynched = Files.exists(path); //synchronizedFiles.contains(path);
	        	ProgressState stateToSet = ProgressState.DEFAULT;
	        	if(failedFiles.contains(path)){
	        		stateToSet = ProgressState.FAILED;
	        	} else if(executingFiles.contains(path)){
	        		stateToSet = ProgressState.IN_PROGRESS;
	        	} else {
	        		stateToSet = ProgressState.SUCCESSFUL;
	        	}

	        	SyncTreeItem item = createItem(topLevelNode.getFile().toPath(),
	        			isSynched, topLevelNode.isFile(), topLevelNode.getUserPermissions());

	        	if(topLevelNode.isShared()){
	        		item.setIsShared(true);
	        	}
	        	putTreeItem(item);
	        	item.setProgressState(stateToSet);
				addChildrensToTreeView(topLevelNode);
			}
		}
	}

	private SyncTreeItem createItem(Path path, boolean isSynched,
			boolean isFile, Set<UserPermission> permissions) {
		PathItem pathItem = new PathItem(path, isFile, permissions);
		SyncTreeItem newItem = new SyncTreeItem(pathItem);
		newItem.updateIsSelectedInUIThread(isSynched);
		newItem.setSelected(isSynched);
		return newItem;
	}

	private SyncTreeItem createItem(Path path, boolean isSynched,
			boolean isFile) {
		Set<UserPermission> defaultPermissions = new HashSet<UserPermission>();
		defaultPermissions.add(new UserPermission(userConfig.getUsername(), PermissionType.WRITE));
		return createItem(path, isSynched, isFile, defaultPermissions);
	}

	private class FileInfoComparator implements Comparator<FileInfo> {
		@Override
		public int compare(FileInfo o1, FileInfo o2) {
			return o1.getPath().compareTo(o2.getPath());
		}
	}

}

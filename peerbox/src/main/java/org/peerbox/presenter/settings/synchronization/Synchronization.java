package org.peerbox.presenter.settings.synchronization;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javafx.util.Callback;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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
import org.peerbox.app.manager.file.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.file.LocalShareFolderMessage;
import org.peerbox.app.manager.file.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.RemoteShareFolderMessage;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionStartedMessage;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionSucceededMessage;
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
	private IUserManager userManager;

	/**
	 * These variables are used to build sets containing the paths of files
	 * and folder whose synchronization state has to be changed in the {@link
	 * org.peerbox.watchservice.FileEventManager FileEventManager}
	 */
	private TreeSet<FileHelper> toSynchronize = new TreeSet<FileHelper>(new FileHelperComparator());
	private TreeSet<FileHelper> toDesynchronize = new TreeSet<FileHelper>(new FileHelperComparator());

	private UserConfig userConfig;

	/**
	 * These variables are build directly from the {@link org.peerbox.
	 * watchservice.FileEventManager FileEventManager} to correctly
	 * populate the TreeView
	 */
	private Set<Path> synchronizedFiles;
	private Set<Path> failedFiles = new HashSet<Path>();
	private Set<Path> executingFiles = new HashSet<Path>();

	private final Provider<IShareFolderHandler> shareFolderHandlerProvider;

	private IFxmlLoaderProvider fxmlLoaderProvider;

	private final Provider<IFileRecoveryHandler> recoverFileHandlerProvider;


	@Inject
	public Synchronization(IFileManager fileManager, FileEventManager eventManager,
			UserConfig userConfig, Provider<IFileRecoveryHandler> recoverFileHandlerProvider, Provider<IShareFolderHandler> shareFolderHandlerProvider) {
		this.eventManager = eventManager;
		this.fileManager = fileManager;
		this.userConfig = userConfig;
		this.recoverFileHandlerProvider = recoverFileHandlerProvider;
		this.shareFolderHandlerProvider = shareFolderHandlerProvider;
		this.fxmlLoaderProvider = fxmlLoaderProvider;
	}

	public Set<FileHelper> getToSynchronize(){
		return toSynchronize;
	}


	public Set<FileHelper> getToDesynchronize(){
		return toDesynchronize;
	}

	public IFileEventManager getFileEventManager(){
		return eventManager;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("Initialize Synchronization!");
		synchronizedFiles = getFileEventManager().getFileTree().getSynchronizedPathsAsSet();
		createTreeViewFromNetwork();
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Accept" button.
	 * @param event
	 */
	public void acceptSyncAction(ActionEvent event) {
		synchronizedFiles = eventManager.getFileTree().getSynchronizedPathsAsSet();

		for(Path path: synchronizedFiles){
			logger.trace("in sync from fem: {}", path);
		}
		for(FileHelper node : toSynchronize){
			logger.trace("Sync the file {}", node.getPath());
			if(!synchronizedFiles.contains(node.getPath()))

				eventManager.onFileSynchronized(node.getPath(), node.isFolder());
		}
		for(FileHelper node: toDesynchronize.descendingSet()){
			eventManager.onFileDesynchronized(node.getPath());
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
	 * @param event
	 */
	@FXML
	public void selectAllAction(ActionEvent event) {
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		root.setSelected(true);
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Unselect all" button.
	 * @param event
	 */
	@FXML
	public void unselectAllAction(ActionEvent event) {
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		root.setSelected(false);
		root.setIndeterminate(false);
	}

	/**
	 * This method is bound to the fxml of the view and is invoked
	 * by clicks on the "Cancel" button.
	 * @param event
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
	 * @param event
	 */
	@FXML
	public void refreshAction(ActionEvent event){
		synchronizedFiles = getFileEventManager().getFileTree().getSynchronizedPathsAsSet();
		failedFiles = getFileEventManager().getFailedOperations();
		createTreeViewFromNetwork();
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.
	 * presenter.settings.synchronization.messages.FileExecutionStartedMessage
	 * FileExecutionStartedMessage} is published using the {@link org.peerbox.
	 * events.MessageBus MessageBus}. This method changes the corresponding
	 * {@link javafx.scene.control.CheckBoxTreeItem CheckBoxTreeItem} in the
	 * {@link javafx.scene.control.TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onExecutionStarts(FileExecutionStartedMessage message) {
		logger.trace("onExecutionStarts: {}", message.getFile().getPath());
		ImageView view;
		CheckBoxTreeItem<PathItem> item = getTreeItem(message.getFile().getPath());

		if(message.getFile().isFile()){
			view = SynchronizationUtils.getFileInProgressIcon();
		} else {
			view = SynchronizationUtils.getFolderInProgressIcon();
			if(item.getValue().getUserPermissions().size() > 1){
				view = SynchronizationUtils.getSharedFolderInProgressIcon();
			} else {
				view = SynchronizationUtils.getFolderInProgressIcon();
			}
		}
		
		if(item == null){
			item = createItem(message.getFile().getPath(), false, message.getFile().isFile());
		    putTreeItem(item);
		    item.setSelected(false);
			logger.trace("item == null for {}", message.getFile().getPath());
		}

		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, SynchronizationUtils.getInProgressToolTip());
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.
	 * manager.file.RemoteFileDeletedMessage RemoteFileDeletedMessage} is
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
	 * This handler is automatically invoked when a {@link org.peerbox.app.
	 * manager.file.RemoteFileMovedMessage RemoteFileMovedMessage} is
	 * published using the {@link org.peerbox.events.MessageBus MessageBus}.
	 * This method changes the corresponding {@link javafx.scene.control.
	 * CheckBoxTreeItem CheckBoxTreeItem} in the {@link javafx.scene.control.
	 * TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onFileRemotelyMoved(RemoteFileMovedMessage message){
		logger.trace("onFileRemotelyMoved: {}", message.getFile().getPath());

		Path srcFile = message.getSourceFile().getPath();
		Path dstFile = message.getDestinationFile().getPath();
		logger.trace("onFileMoved: {} --> {}", srcFile, dstFile);

		removeTreeItemInUIThread(srcFile);
		CheckBoxTreeItem<PathItem> item = getOrCreateItem(message.getFile(), true);
		ImageView view = null;
		if(message.getFile().isFile()){
			view = SynchronizationUtils.getFileSuccessIcon();
		} else {
			view = SynchronizationUtils.getFolderSuccessIcon();
		}

		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, SynchronizationUtils.getSuccessTooltip());
		updateIsSelectedInUIThread(item, message.getFile(), true);
	}


	/**
	 * This handler is automatically invoked when a {@link org.peerbox.
	 * presenter.settings.synchronization.messages.FileExecutionSucceededMessage
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

		String tooltip = SynchronizationUtils.getSuccessTooltip();
		switch(stateType){
		case LOCAL_HARD_DELETE:
			removeTreeItemInUIThread(message.getFile().getPath());
			break;

		case INITIAL:
			CheckBoxTreeItem<PathItem> item = getOrCreateItem(message.getFile(), true);
			ImageView view;
			tooltip = SynchronizationUtils.getSuccessTooltip();
			if(message.getFile().isFile()){
				view = SynchronizationUtils.getFileSuccessIcon();
			} else {
				if(item.getValue().getUserPermissions().size() > 1){
					view = SynchronizationUtils.getSharedFolderSuccessIcon();
				} else {
					view = SynchronizationUtils.getFolderSuccessIcon();
					tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
				}
			}

			updateIconInUIThread(item, view);
			updateTooltipInUIThread(item, tooltip);
			break;
		case LOCAL_MOVE:
			Path srcFile = message.getSourceFile().getPath();
			Path dstFile = message.getFile().getPath();
			logger.trace("onFileMoved: {} --> {}", srcFile, dstFile);

			removeTreeItemInUIThread(srcFile);
			item = getOrCreateItem(message.getFile(), true);
			tooltip = SynchronizationUtils.getSuccessTooltip();
			if(message.getFile().isFile()){
				view = SynchronizationUtils.getFileSuccessIcon();
			} else {
				
				if(item.getValue().getUserPermissions().size() > 1){
					tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
					view = SynchronizationUtils.getSharedFolderSuccessIcon();
				} else {
					view = SynchronizationUtils.getFolderSuccessIcon();
				}
			}

			updateIconInUIThread(item, view);
			updateTooltipInUIThread(item, tooltip);
			selectFilesAndFolderInUIThread(item, message.getFile());
			break;
		default:
			item = getOrCreateItem(message.getFile(), true);
			tooltip = SynchronizationUtils.getSuccessTooltip();
			if(message.getFile().isFile()){
				view = SynchronizationUtils.getFileSuccessIcon();
			} else {
				if(item.getValue().getUserPermissions().size() > 1){
					view = SynchronizationUtils.getSharedFolderSuccessIcon();
					tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
				} else {
					view = SynchronizationUtils.getFolderSuccessIcon();
				}
			}

			updateIconInUIThread(item, view);
			updateTooltipInUIThread(item, tooltip);
			updateIsSelectedInUIThread(item, message.getFile(), true);
		}
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.
	 * presenter.settings.synchronization.messages.FileExecutionFailedMessage
	 * FileExecutionFailedMessage} is published using the {@link org.peerbox.
	 * events.MessageBus MessageBus}. This method changes the corresponding
	 * {@link javafx.scene.control. CheckBoxTreeItem CheckBoxTreeItem} in the
	 * {@link javafx.scene.control.TreeView TreeView} accordingly.
	 */
	@Override
	@Handler
	public void onExecutionFails(FileExecutionFailedMessage message) {
		logger.trace("onExecutionFails: {}", message.getFile().getPath());
		
		CheckBoxTreeItem<PathItem> item = getOrCreateItem(message.getFile(), false);
		String tooltip = SynchronizationUtils.getErrorTooltip();
		ImageView view;
		if(message.getFile().isFile()){
			view = SynchronizationUtils.getFileErrorIcon();
		} else {
			if(item.getValue().getUserPermissions().size() > 1){
				tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
				view = SynchronizationUtils.getSharedFolderErrorIcon();
			} else {
				view = SynchronizationUtils.getFolderErrorIcon();
			}
		}

		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, tooltip);

		final CheckBoxTreeItem<PathItem> item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	item2.setSelected(true);
	        }
		});
	}

	/**
	 * This handler is automatically invoked when a {@link org.peerbox.app.
	 * manager.file.LocalFileDesyncMessage LocalFileDesyncMessage} is published
	 * using the {@link org.peerbox.events.MessageBus MessageBus}. This method
	 * changes the corresponding {@link javafx.scene.control. CheckBoxTreeItem
	 * CheckBoxTreeItem} in the {@link javafx.scene.control.TreeView TreeView}
	 * accordingly.
	 */
	@Override
	@Handler
	public void onFileSoftDeleted(LocalFileDesyncMessage message) {
		logger.trace("onFileSoftDeleted: {}", message.getFile().getPath());

		String tooltip = SynchronizationUtils.getSoftDeletedTooltip();
		CheckBoxTreeItem<PathItem> item = getOrCreateItem(message.getFile(), false);
		ImageView view;
		if(message.getFile().isFile()){
			view = SynchronizationUtils.getFileStandardIcon();
		} else {
			
			if(item.getValue().getUserPermissions().size() > 1){
				view = SynchronizationUtils.getSharedFolderStandardIcon();
				tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
			} else {
				view = SynchronizationUtils.getFolderStandardIcon();
			}
		}

		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, tooltip);
		final CheckBoxTreeItem<PathItem> item2 = item;
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
		
		CheckBoxTreeItem<PathItem> item = getOrCreateItem(message.getFile(), false);

		ImageView view = SynchronizationUtils.getSharedFolderStandardIcon();
		String tooltip = "";
		if(item.getGraphic() != null && item.getGraphic() instanceof Label){
			Label oldLabel = (Label) item.getGraphic();
			tooltip = oldLabel.getTooltip().getText();
		}
		tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());

		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, tooltip);
//		forceUpdateCheckBoxTreeItem(item);
	}

	@Override
	@Handler
	public void onLocalFolderShared(LocalShareFolderMessage message) {
		logger.trace("onLocalFolderShared: {}", message.getFile().getPath());
		
		CheckBoxTreeItem<PathItem> item = getOrCreateItem(message.getFile(), false);
		ImageView view = SynchronizationUtils.getSharedFolderSuccessIcon();

		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, SynchronizationUtils.getSharedFolderTooltip());
		final CheckBoxTreeItem<PathItem> item2 = item;
	}

	private void removeTreeItemInUIThread(Path srcFile) {
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	removeTreeItem(srcFile);
	        }
		});
	}

	private void updateIsSelectedInUIThread(CheckBoxTreeItem<PathItem> item, FileHelper file,
			boolean b) {
		final CheckBoxTreeItem<PathItem> item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	if(file.getPath().toFile().isDirectory()){
	        		item2.setIndeterminate(b);
	        	} else {
	        		item2.setSelected(b);
	        	}
	        }
		});
	}

	private void selectFilesAndFolderInUIThread(CheckBoxTreeItem<PathItem> item, FileHelper file){
		final CheckBoxTreeItem<PathItem> item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	item2.setSelected(true);
	        }
		});
	}

	private CheckBoxTreeItem<PathItem> getOrCreateItem(FileHelper file, boolean isSynched) {
		// TODO Auto-generated method stub
		CheckBoxTreeItem<PathItem> item = getTreeItem(file.getPath());

		if(item != null){
			logger.trace("item != null for {}, change icon!", file.getPath());
		} else {
			item = createItem(file.getPath(), true, file.isFile());
			putTreeItem(item);
			logger.trace("item == null for {}", file.getPath());
		}
		return item;
	}

	private CheckBoxTreeItem<PathItem> getTreeItem(Path path){
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		return findTreeItem(root, path, false);
	}

	private void putTreeItem(CheckBoxTreeItem<PathItem> item){
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		Path pathLeft = prefix.relativize(item.getValue().getPath());
		putTreeItem(root, item, pathLeft);
		return;
	}

	private CheckBoxTreeItem<PathItem> removeTreeItem(Path path){
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		return findTreeItem(root, path, true);

	}

	private CheckBoxTreeItem<PathItem> findTreeItem(CheckBoxTreeItem<PathItem> item, Path path, boolean remove){
		Path wholePath = path;
		Path prefix = item.getValue().getPath();
		if(path.startsWith(prefix)){
			path = prefix.relativize(wholePath);
		}
		if(path.equals(Paths.get(""))){
			if(remove){
				item.getParent().getChildren().remove(item);
			}
			return item;
		}
		Path nextLevel = path.getName(0);
		ObservableList<TreeItem<PathItem>> children = item.getChildren();

		for(TreeItem<PathItem> child : item.getChildren()){
			CheckBoxTreeItem<PathItem> castedChild = (CheckBoxTreeItem<PathItem>)child;
			Path childNextLevel = prefix.relativize(child.getValue().getPath()).getName(0);
			if(childNextLevel.equals(nextLevel)){
				 return findTreeItem(castedChild, wholePath, remove);
			 }
		}
		return null;
	}

	private void putTreeItem(CheckBoxTreeItem<PathItem> parent, CheckBoxTreeItem<PathItem> toPut, Path pathLeft){
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
			return;
		} else {
			Iterator<TreeItem<PathItem>> iter = parent.getChildren().iterator();
			Path nextLevel = pathLeft.getName(0);
			Path pathToSearch = parentPath.resolve(nextLevel);
			while(iter.hasNext()){
				CheckBoxTreeItem<PathItem> child = (CheckBoxTreeItem<PathItem>)iter.next();
				if(child.getValue().getPath().equals(pathToSearch)){
					putTreeItem(child, toPut, child.getValue().getPath().relativize(wholePath));
					return;
				}
			}
			PathItem pathItem = new PathItem(pathToSearch, toPut.isSelected(), null);
			CheckBoxTreeItem<PathItem> created = new CheckBoxTreeItem<PathItem>(pathItem);
			toPut.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler(this));
			parent.getChildren().add(toPut);
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
		CheckBoxTreeItem<PathItem> invisibleRoot = new CheckBoxTreeItem<PathItem>(pathItem);
	    fileTreeView.setRoot(invisibleRoot);
        fileTreeView.setEditable(false);
        fileTreeView.setCellFactory(new Callback<TreeView<PathItem>, TreeCell<PathItem>>(){
            @Override
            public TreeCell<PathItem> call(TreeView<PathItem> p) {
                return new CustomizedTreeCell(getFileEventManager(),
                		recoverFileHandlerProvider,
                		shareFolderHandlerProvider);
            }
        });

        fileTreeView.setShowRoot(false);

        addChildrensToTreeView(fileNode);
	}

	private void addChildrensToTreeView(FileNode fileNode){
		if(fileNode.getChildren() != null){
	        for(FileNode topLevelNode : fileNode.getChildren()){
				ImageView icon;
	        	String tooltip;

	        	Path path = topLevelNode.getFile().toPath();
	        	boolean isSynched = synchronizedFiles.contains(path);

	        	if(failedFiles.contains(path)){
	        		tooltip = SynchronizationUtils.getErrorTooltip();
	        		if(topLevelNode.isFile()){
	        			icon = SynchronizationUtils.getFileErrorIcon();
	        		} else {
	        			if(topLevelNode.getUserPermissions().size() > 1){
	        				icon = SynchronizationUtils.getSharedFolderErrorIcon();
	        			} else {
	        				icon = SynchronizationUtils.getFolderErrorIcon();
	        				tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
	        			}
	        		}
	        		
	        	} else if(executingFiles.contains(path)){
	        		tooltip = SynchronizationUtils.getInProgressToolTip();
	        		if(topLevelNode.isFile()){
	        			icon = SynchronizationUtils.getFileInProgressIcon();//;new ImageView(inProgressIcon);
	        		} else {
	        			if(topLevelNode.getUserPermissions().size() > 1){
	        				icon = SynchronizationUtils.getSharedFolderInProgressIcon();
	        			} else {
	        				icon = SynchronizationUtils.getFolderInProgressIcon();
	        				tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
	        			}
	        		}
	        		
	        	} else {
	        		tooltip = SynchronizationUtils.getSuccessTooltip();
	        		if(topLevelNode.isFile()){
	        			icon = SynchronizationUtils.getFileSuccessIcon();
	        		} else {
	        			if(topLevelNode.getUserPermissions().size() > 1){
	        				icon = SynchronizationUtils.getSharedFolderSuccessIcon();
	        			} else {
	        				icon = SynchronizationUtils.getFolderSuccessIcon();
	        				tooltip = tooltip.concat(SynchronizationUtils.getSharedFolderTooltip());
	        			}
	        		}
	        	}

	        	CheckBoxTreeItem<PathItem> item = createItem(topLevelNode.getFile().toPath(),
	        			isSynched, topLevelNode.isFile(), topLevelNode.getUserPermissions());
	        	putTreeItem(item);
	        	updateIconInUIThread(item, icon);
	        	updateTooltipInUIThread(item, tooltip);

				addChildrensToTreeView(topLevelNode);
			}
		}
	}

	private CheckBoxTreeItem<PathItem> createItem(Path path, boolean isSynched,
			boolean isFile, Set<UserPermission> permissions) {
		PathItem pathItem = new PathItem(path, isFile, permissions);
		Label label = new Label(path.getFileName().toString());
		CheckBoxTreeItem<PathItem> newItem = new CheckBoxTreeItem<PathItem>(pathItem, label);
		newItem.setSelected(isSynched);
		return newItem;
	}
	
	private CheckBoxTreeItem<PathItem> createItem(Path path, boolean isSynched,
			boolean isFile) {
		Set<UserPermission> defaultPermissions = new HashSet<UserPermission>();
		defaultPermissions.add(new UserPermission(userConfig.getUsername(), PermissionType.WRITE));
		return createItem(path, isSynched, isFile, defaultPermissions);
	}

	private void updateIconInUIThread(TreeItem<PathItem> item, ImageView icon){
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	if(item.getGraphic() != null && item.getGraphic() instanceof Label){
	        		Label oldLabel = (Label)item.getGraphic();
	        		oldLabel.setGraphic(icon);
	        		item.setGraphic(oldLabel);
	        	} else {
	        		Label newLabel = new Label(item.getValue().getPath().getFileName().toString());
	        		newLabel.setGraphic(icon);
	        		item.setGraphic(newLabel);
	        	}
	        }
		});
	}

	private void updateTooltipInUIThread(TreeItem<PathItem> item, String tooltip){
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	if(item.getGraphic() != null && item.getGraphic() instanceof Label){
	        		Label oldLabel = (Label)item.getGraphic();
	        		oldLabel.setTooltip(new Tooltip(tooltip));
	        		item.setGraphic(oldLabel);
	        	} else {
	        		Label newLabel = new Label(item.getValue().getPath().getFileName().toString());
	        		newLabel.setTooltip(new Tooltip(tooltip));
	        		item.setGraphic(newLabel);
	        	}
	        }
		});
	}

	private class FileHelperComparator implements Comparator<FileHelper>{
		@Override
		public int compare(FileHelper o1, FileHelper o2) {
			String path1 = o1.getPath().toString();
			String path2 = o2.getPath().toString();
			return path1.compareTo(path2);
		}
	}
	
}
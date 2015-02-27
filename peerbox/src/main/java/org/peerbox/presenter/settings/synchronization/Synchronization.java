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
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.file.LocalFileMovedMessage;
import org.peerbox.app.manager.file.RemoteFileAddedMessage;
import org.peerbox.app.manager.file.RemoteFileDeletedMessage;
import org.peerbox.presenter.settings.synchronization.eventbus.IExecutionMessageListener;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionStartedMessage;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionSucceededMessage;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.states.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

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
	private TreeSet<FileHelper> toSynchronize = new TreeSet<FileHelper>(new FileHelperComparator());
	private TreeSet<FileHelper> toDesynchronize = new TreeSet<FileHelper>(new FileHelperComparator());
	private UserConfig userConfig;
	
	private Set<Path> synchronizedFiles;
	private Set<Path> failedFiles = new HashSet<Path>();
	private Set<Path> executingFiles = new HashSet<Path>();

	@Inject
	public Synchronization(IFileManager fileManager, FileEventManager eventManager, UserConfig userConfig) {
		this.eventManager = eventManager;
		this.fileManager = fileManager;
		this.userConfig = userConfig;
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
		synchronizedFiles = eventManager.getFileTree().getSynchronizedPathsAsSet();
		createTreeViewFromNetwork();
	}

	

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

	@FXML
	public void selectAllAction(ActionEvent event) {
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		root.setSelected(true);
	}

	@FXML
	public void unselectAllAction(ActionEvent event) {
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		root.setSelected(false);
		root.setIndeterminate(false);
	}

	@FXML
	public void cancelAction(ActionEvent event) {
		if(event.getTarget() != null && event.getTarget() instanceof Button){
			Button cancelButton = (Button)event.getTarget();
			Window window = cancelButton.getScene().getWindow();
			window.hide();
		}
	}

	@FXML
	public void refreshAction(ActionEvent event){
		synchronizedFiles = eventManager.getFileTree().getSynchronizedPathsAsSet();
		failedFiles = eventManager.getFailedOperations();
		createTreeViewFromNetwork();
	}

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
		}
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getFile().getPath());
		} else {
			item = createItem(message.getFile().getPath(), false, message.getFile().isFile());
		    putTreeItem(item);
		    item.setSelected(false);
			logger.trace("item == null for {}", message.getFile().getPath());
		}
		
		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, SynchronizationUtils.getInProgressToolTip());
	}
	
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
			CheckBoxTreeItem<PathItem> item = getOrCreateItem(message.getFile());
		
			ImageView view;
			if(message.getFile().isFile()){
				view = SynchronizationUtils.getFileSuccessIcon();
			} else {
				view = SynchronizationUtils.getFolderSuccessIcon();
			}
			
			updateIconInUIThread(item, view);
			updateTooltipInUIThread(item, SynchronizationUtils.getSuccessTooltip());
			break;
		case LOCAL_MOVE:
			Path srcFile = message.getSourceFile().getPath();
			Path dstFile = message.getFile().getPath();
			logger.trace("onFileMoved: {} --> {}", srcFile, dstFile);	
			
			removeTreeItemInUIThread(srcFile);
			item = getOrCreateItem(message.getFile());
			
			if(message.getFile().isFile()){
				view = SynchronizationUtils.getFileSuccessIcon();
			} else {
				view = SynchronizationUtils.getFolderSuccessIcon();
			}
			
			updateIconInUIThread(item, view);
			updateTooltipInUIThread(item, SynchronizationUtils.getSuccessTooltip());
			updateIsSelectedInUIThread(item, message.getFile(), true);
			break;
		default:
			item = getOrCreateItem(message.getFile());
			
			if(message.getFile().isFile()){
				view = SynchronizationUtils.getFileSuccessIcon();
			} else {
				view = SynchronizationUtils.getFolderSuccessIcon();
			}
			
			updateIconInUIThread(item, view);
			updateTooltipInUIThread(item, SynchronizationUtils.getSuccessTooltip());
			updateIsSelectedInUIThread(item, message.getFile(), true);
		}
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

	private CheckBoxTreeItem<PathItem> getOrCreateItem(FileHelper file) {
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

	@Override
	@Handler
	public void onExecutionFails(FileExecutionFailedMessage message) {
		logger.trace("onExecutionFails: {}", message.getFile().getPath());
		CheckBoxTreeItem<PathItem> item = getTreeItem(message.getFile().getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getFile().getPath());
		} else {
			item = createItem(message.getFile().getPath(), false, message.getFile().isFile());
			putTreeItem(item);
			logger.trace("item == null for {}", message.getFile().getPath());
		}
		
		ImageView view;
		if(message.getFile().isFile()){
			view = SynchronizationUtils.getFileErrorIcon();
		} else {
			view = SynchronizationUtils.getFolderErrorIcon();
		}
		
		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, SynchronizationUtils.getErrorTooltip());
		
		final CheckBoxTreeItem<PathItem> item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	item2.setSelected(true);
	        }
		});
	}
	
	@Override
	@Handler
	public void onFileSoftDeleted(LocalFileDesyncMessage message) {
		logger.trace("onFileSoftDeleted: {}", message.getFile().getPath());
		Path path = message.getFile().getPath();
		
		CheckBoxTreeItem<PathItem> item = getTreeItem(message.getFile().getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getFile().getPath());
		} else {
			createItem(message.getFile().getPath(), false, message.getFile().isFile());
			putTreeItem(item);
			item.setSelected(false);
			logger.trace("item == null for {}", message.getFile().getPath());
		}

		ImageView view;
		if(message.getFile().isFile()){
			view = SynchronizationUtils.getFileStandardIcon();
		} else {
			view = SynchronizationUtils.getFolderStandardIcon();
		}
		
		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, SynchronizationUtils.getSoftDeletedTooltip());
		final CheckBoxTreeItem<PathItem> item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	item2.setSelected(false);
	        }
	   });
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
			toPut.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler());
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
			PathItem pathItem = new PathItem(pathToSearch, toPut.isSelected());
			CheckBoxTreeItem<PathItem> created = new CheckBoxTreeItem<PathItem>(pathItem);
			toPut.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler());
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
		PathItem pathItem = new PathItem(userConfig.getRootPath(), false);
		CheckBoxTreeItem<PathItem> invisibleRoot = new CheckBoxTreeItem<PathItem>(pathItem);
	    fileTreeView.setRoot(invisibleRoot);
        fileTreeView.setEditable(false);
        fileTreeView.setCellFactory(new Callback<TreeView<PathItem>, TreeCell<PathItem>>(){
            @Override
            public TreeCell<PathItem> call(TreeView<PathItem> p) {
                return new CustomizedTreeCell(eventManager);
            }
        });
        
        fileTreeView.setShowRoot(false);
        
        addFileNodeToTreeView(fileNode);
	}
	
	private void addFileNodeToTreeView(FileNode fileNode){
		if(fileNode.getChildren() != null){
	        for(FileNode topLevelNode : fileNode.getChildren()){
				ImageView icon;
	        	String tooltip;
	        	
	        	Path path = topLevelNode.getFile().toPath();
	        	boolean isSynched = synchronizedFiles.contains(path);
	        	
	        	if(failedFiles.contains(path)){
	        		if(topLevelNode.isFile()){
	        			icon = SynchronizationUtils.getFileErrorIcon();
	        		} else {
	        			icon = SynchronizationUtils.getFolderErrorIcon();
	        		}
	        		tooltip = SynchronizationUtils.getErrorTooltip();
	        	} else if(executingFiles.contains(path)){
	        		if(topLevelNode.isFile()){
	        			icon = SynchronizationUtils.getFileInProgressIcon();//;new ImageView(inProgressIcon);
	        		} else {
	        			icon = SynchronizationUtils.getFolderInProgressIcon();
	        		}
	        		tooltip = SynchronizationUtils.getInProgressToolTip();
	        	} else {
	        		if(topLevelNode.isFile()){
	        			icon = SynchronizationUtils.getFileSuccessIcon();
	        		} else {
	        			icon = SynchronizationUtils.getFolderSuccessIcon();
	        		}
	        		tooltip = SynchronizationUtils.getSuccessTooltip();
	        	}
	        	
	        	CheckBoxTreeItem<PathItem> item = createItem(topLevelNode.getFile().toPath(), isSynched, topLevelNode.isFile());
	        	putTreeItem(item);
	        	updateIconInUIThread(item, icon);
	        	updateTooltipInUIThread(item, tooltip);

				addFileNodeToTreeView(topLevelNode);
			}
		}
	}

	private void forceUpdateCheckBoxTreeItem(CheckBoxTreeItem<PathItem> item){
		PathItem pathItem = item.getValue();
		item.setValue(null);
		item.setValue(pathItem);
	}
	
	private CheckBoxTreeItem<PathItem> createItem(Path path, boolean isSynched,
			boolean isFile) {
		PathItem pathItem = new PathItem(path, isFile);
		Label label = new Label(path.getFileName().toString());
		CheckBoxTreeItem<PathItem> newItem = new CheckBoxTreeItem<PathItem>(pathItem, label);
		newItem.setSelected(isSynched);
		return newItem;
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

	private class ClickEventHandler implements EventHandler<TreeModificationEvent<PathItem>>{

		@Override
		public void handle(TreeModificationEvent<PathItem> arg0) {
			CheckBoxTreeItem<PathItem> source = (CheckBoxTreeItem<PathItem>) arg0.getSource();
			Path path = source.getValue().getPath();
			FileHelper file = new FileHelper(source.getValue().getPath(), source.getValue().isFile());
//			if(source.isSelected() || source.isIndeterminate()){
			if(source.isSelected()){
				logger.trace("Add {} to SYNC", path);
				getToSynchronize().add(file);
				getToDesynchronize().remove(file);
			} else if(!source.isIndeterminate()){
				logger.trace("Remove {} from SYNC", path);
				getToSynchronize().remove(file);
				getToDesynchronize().add(file);

				ImageView view;
				if(file.isFile()){
					view = SynchronizationUtils.getFileStandardIcon();
				} else {
					view = SynchronizationUtils.getFolderStandardIcon();
				}
				
				updateIconInUIThread(source, view);
				updateTooltipInUIThread(source, SynchronizationUtils.getSoftDeletedTooltip());
			}
			arg0.consume();
		}
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
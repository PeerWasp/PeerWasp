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
import java.util.Vector;

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
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.scene.control.TreeCell;
import net.engio.mbassy.listener.Handler;

import org.controlsfx.tools.Platform;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.presenter.settings.synchronization.eventbus.IExecutionMessageListener;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionStartedMessage;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionSucceededMessage;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
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
	
	private final Image inProgressIcon = new Image(getClass().getResourceAsStream("/images/file-synch.png"));
	private final Image successIcon = new Image(getClass().getResourceAsStream("/images/file-success.png"));
	private final Image errorIcon = new Image(getClass().getResourceAsStream("/images/file-error.png"));
	private final Image standardIcon = new Image(getClass().getResourceAsStream("/images/file-standard.png"));
	
	private final Image inProgressFolderIcon = new Image(getClass().getResourceAsStream("/images/folder-synch.png"));
	private final Image successFolderIcon = new Image(getClass().getResourceAsStream("/images/folder-success.png"));
	private final Image errorFolderIcon = new Image(getClass().getResourceAsStream("/images/folder-error.png"));
	private final Image standardFolderIcon = new Image(getClass().getResourceAsStream("/images/folder-standard.png"));
	
	private final String successTooltip = "This file is synchronized.";
	private final String errorTooltip = "Synchronization of this file failed.";
	private final String inProgressTooltip = "Synchronization of this file is currently ongoing.";
	private final String softDeletedTooltip = "This file is currently not synchronized.\n"
			+ "Check the box to download it.";

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
		createTreeWithFilesFromNetwork();
	}

	private void createTreeWithFilesFromNetwork() {
		try {
			FileNode filesFromNetwork = fileManager.listFiles().execute();
			if(filesFromNetwork != null){
				listFiles(filesFromNetwork);
			} else {
				logger.trace("Files from network are null");
			}
		} catch (NoSessionException | NoPeerConnectionException
				| InvalidProcessStateException | ProcessExecutionException e) {
			e.printStackTrace();
		}
	}

	private void listFiles(FileNode fileNode){
		PathItem pathItem = new PathItem(userConfig.getRootPath(), false);
//		fileTreeView = new TreeView<PathItem>();
		CheckBoxTreeItem<PathItem> invisibleRoot = new CheckBoxTreeItem<PathItem>(pathItem);
	//	fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());
	    fileTreeView.setRoot(invisibleRoot);
        fileTreeView.setEditable(false);
        
        fileTreeView.setCellFactory(new Callback<TreeView<PathItem>, TreeCell<PathItem>>(){
            @Override
            public TreeCell<PathItem> call(TreeView<PathItem> p) {
                return new CustomizedTreeCell(eventManager);
            }
        });
        
        fileTreeView.setShowRoot(false);
        createTreeFromFileNode(fileNode);
	}
	
	private void createTreeFromFileNode(FileNode fileNode){
		if(fileNode.getChildren() != null){
	        for(FileNode topLevelNode : fileNode.getChildren()){
				ImageView view;
	        	Path path = topLevelNode.getFile().toPath();
	        	String tooltip;
	        	boolean isSynched = synchronizedFiles.contains(path);
	        	
	        	if(failedFiles.contains(path)){
	        		if(topLevelNode.isFile()){
	        			view = new ImageView(errorIcon);
	        		} else {
	        			view = new ImageView(errorFolderIcon);
	        		}
	        		tooltip = errorTooltip;
	        	} else if(executingFiles.contains(path)){
	        		if(topLevelNode.isFile()){
	        			view = new ImageView(inProgressIcon);
	        		} else {
	        			view = new ImageView(inProgressIcon);
	        		}
	        		tooltip = inProgressTooltip;
	        	} else {
	        		if(topLevelNode.isFile()){
	        			view = new ImageView(successIcon);
	        		} else {
	        			view = new ImageView(successFolderIcon);
	        		}
	        		tooltip = successTooltip;
	        	}
	        	
	        	CheckBoxTreeItem<PathItem> item = putTreeItem(topLevelNode.getFile().toPath(), isSynched, topLevelNode.isFile());
	        	logger.trace("Put item with path {}", topLevelNode.getFile().toPath());

	        	updateIconInUIThread(item, view);
	        	updateTooltipInUIThread(item, tooltip);
				forceUpdateTreeItem(item);
				createTreeFromFileNode(topLevelNode);
			}
		}
	}

	public void acceptSyncAction(ActionEvent event) {
		synchronizedFiles = eventManager.getFileTree().getSynchronizedPathsAsSet();

		for(FileHelper node : toSynchronize){
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
		createTreeWithFilesFromNetwork();
	}


	@Override
	@Handler
	public void onExecutionStarts(FileExecutionStartedMessage message) {
		logger.trace("onExecutionStarts: {}", message.getFile().getPath());
		ImageView view = new ImageView(inProgressIcon);
		TreeItem<PathItem> item = getTreeItem(message.getFile().getPath());
		
		if(message.getFile().isFile()){
			view = new ImageView(inProgressIcon);
		} else {
			view = new ImageView(inProgressFolderIcon);
		}
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getFile().getPath());
		} else {
		    item = putTreeItem(message.getFile().getPath(), false, message.getFile().isFile());
			logger.trace("item == null for {}", message.getFile().getPath());
		}
		
//		setIconAndTooltip(item, view, "This file is synchronized. Right-click to delete it.");
		
		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, inProgressTooltip);
		forceUpdateTreeItem(item);
		
//		item.setGraphic(view);
	}

	@Override
	@Handler
	public void onExecutionSucceeds(FileExecutionSucceededMessage message) {
		StateType stateType = message.getStateType();
		
		switch(stateType){
		case LOCAL_HARD_DELETE:
			javafx.application.Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
		        	removeTreeItem(message.getFile().getPath());
		        }
			});
			break;
		default:
	
			logger.trace("onExecutionSucceeds: {}", message.getFile().getPath());
			CheckBoxTreeItem<PathItem> item = getTreeItem(message.getFile().getPath());
			
			if(item != null){
				logger.trace("item != null for {}, change icon!", message.getFile().getPath());
			} else {
				item = putTreeItem(message.getFile().getPath(), true, message.getFile().isFile());
				logger.trace("item == null for {}", message.getFile().getPath());
			}
//			setIconAndTooltip(item, view, "This file is synchronized. Right-click to delete it.");
			
			ImageView view;
			if(message.getFile().isFile()){
				view = new ImageView(successIcon);
			} else {
				view = new ImageView(successFolderIcon);
			}
			
			updateIconInUIThread(item, view);
			updateTooltipInUIThread(item, successTooltip);
			
			final CheckBoxTreeItem<PathItem> item2 = item;
			javafx.application.Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
		        	if(message.getFile().getPath().toFile().isDirectory()){
		        		item2.setIndeterminate(true);
		        	} else {
		        		item2.setSelected(true);
		        	}
		        	
		        }
			});
			
//			forceUpdateTreeItem(item);
		}
	}
//	
//	private void setIconAndTooltip(TreeItem<PathItem> item, ImageView icon, String tooltip){
//		javafx.application.Platform.runLater(new Runnable() {
//	        @Override
//	        public void run() {
//	        	Label label = new Label(item.getValue().getPath().getFileName().toString());
//	        	label.setGraphic(icon);
//	        	label.setTooltip(new Tooltip("This file is synchronized. Right-click to delete it."));
//	        	item.setGraphic(label);  
//	        }
//		});
//	}
	
	private void updateIconInUIThread(TreeItem<PathItem> item, ImageView icon){
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	if(item.getGraphic() != null && item.getGraphic() instanceof Label){
	        		Label oldLabel = (Label)item.getGraphic();
	        		oldLabel.setGraphic(icon);
	        		Tooltip oldTooltip = oldLabel.getTooltip();
//	        		oldLabel.setText(item.getValue().getPath().getFileName().toString());
	        		//item.setGraphic(oldLabel);
//	        		Label newLabel = new Label(item.getValue().getPath().getFileName().toString());
//	        		newLabel.setGraphic(icon);
//	        		newLabel.setTooltip(oldTooltip);
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
	        		logger.trace("Set oldLabel tooltip: {}", tooltip);
	        		oldLabel.setTooltip(new Tooltip(tooltip));
//	        		oldLabel.setText(item.getValue().getPath().getFileName().toString());
	        		//item.setGraphic(oldLabel);
	        		item.setGraphic(oldLabel);
	        	} else {
	        		logger.trace("Set newLabel tooltip: {}", tooltip);
	        		Label newLabel = new Label(item.getValue().getPath().getFileName().toString());
	        		newLabel.setTooltip(new Tooltip(tooltip));
	        		item.setGraphic(newLabel);
	        	}
	        }
		});
	}
	
	@Override
	@Handler
	public void onExecutionFails(FileExecutionFailedMessage message) {
		logger.trace("onExecutionFails: {}", message.getFile().getPath());
		CheckBoxTreeItem<PathItem> item = getTreeItem(message.getFile().getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getFile().getPath());
		} else {
			item = putTreeItem(message.getFile().getPath(), false, message.getFile().isFile());
			logger.trace("item == null for {}", message.getFile().getPath());
		}
		
		
		ImageView view;
		if(message.getFile().isFile()){
			view = new ImageView(errorIcon);
		} else {
			view = new ImageView(errorFolderIcon);
		}
		
		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, errorTooltip);
//		forceUpdateTreeItem(item);
		
		item.setSelected(true);
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
			item = putTreeItem(message.getFile().getPath(), false, message.getFile().isFile());
			logger.trace("item == null for {}", message.getFile().getPath());
		}

		ImageView view;
		if(message.getFile().isFile()){
			view = new ImageView(standardIcon);
		} else {
			view = new ImageView(standardFolderIcon);
		}
		
		updateIconInUIThread(item, view);
		updateTooltipInUIThread(item, softDeletedTooltip);
//		forceUpdateTreeItem(item);
		final CheckBoxTreeItem<PathItem> item2 = item;
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	item2.setSelected(false);
	        }
	   });
	}
	
	private void forceUpdateTreeItem(TreeItem<PathItem> item){
		PathItem value = item.getValue();
		item.setValue(null);
		item.setValue(value);
	}
	
	private CheckBoxTreeItem<PathItem> getTreeItem(Path path){
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		return findTreeItem(root, path, false);
	}

	private CheckBoxTreeItem<PathItem> putTreeItem(Path path, boolean isSynched, boolean isFile){
		CheckBoxTreeItem<PathItem> root = (CheckBoxTreeItem<PathItem>)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		Path pathLeft = prefix.relativize(path);
		return putTreeItem(root, pathLeft, isSynched, isFile);
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
			logger.trace("Successful! {}", item.getValue().getPath());
			if(remove){
				item.getParent().getChildren().remove(item);
			}
			return item;
		}
		Path nextLevel = path.getName(0);
		ObservableList<TreeItem<PathItem>> children = item.getChildren();

		for(TreeItem<PathItem> child : item.getChildren()){
			CheckBoxTreeItem<PathItem> castedChild = (CheckBoxTreeItem<PathItem>)child;
			logger.trace("check child {} of {}", castedChild.getValue().getPath(), item.getValue().getPath());
			
			Path childNextLevel = prefix.relativize(child.getValue().getPath()).getName(0);
			logger.trace("childNextLevel: {} nextLevel {}", childNextLevel, nextLevel);
			if(childNextLevel.equals(nextLevel)){
				 logger.trace("Found as next level {} for {}", item.getValue().getPath(), wholePath);
				 return findTreeItem(castedChild, wholePath, remove);
			 }
		}
		return null;
	}
	
	private CheckBoxTreeItem<PathItem> putTreeItem(CheckBoxTreeItem<PathItem> parent, Path pathLeft, boolean isSynched, boolean isFile){
		Path parentPath = parent.getValue().getPath();
		Path wholePath = parentPath.resolve(pathLeft);

		if(pathLeft.getNameCount() == 1){
			PathItem pathItem = new PathItem(wholePath, isFile);
			Label label = new Label(wholePath.getFileName().toString());
			CheckBoxTreeItem<PathItem> created = new CheckBoxTreeItem<PathItem>(pathItem, label);
			created.setSelected(isSynched);
			//created.setGraphic(created.getValue());
			
			created.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler());
			Iterator<TreeItem<PathItem>> iter = parent.getChildren().iterator();
			while(iter.hasNext()){
				if(iter.next().getValue().getPath().equals(wholePath)){
					iter.remove();
				}
			}
			parent.getChildren().add(created);
			return created;
		} else {
			Iterator<TreeItem<PathItem>> iter = parent.getChildren().iterator();
			Path nextLevel = pathLeft.getName(0);
			Path pathToSearch = parentPath.resolve(nextLevel);
			while(iter.hasNext()){
				CheckBoxTreeItem<PathItem> child = (CheckBoxTreeItem<PathItem>)iter.next();
				if(child.getValue().getPath().equals(pathToSearch)){
					return putTreeItem(child, child.getValue().getPath().relativize(wholePath), isSynched, isFile);
				}
			}
			PathItem pathItem = new PathItem(pathToSearch, isSynched);
			CheckBoxTreeItem<PathItem> created = new CheckBoxTreeItem<PathItem>(pathItem);
			created.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler());
			parent.getChildren().add(created);
			return putTreeItem(created, created.getValue().getPath().relativize(wholePath), isSynched, isFile);
		}
	}

	private class ClickEventHandler implements EventHandler<TreeModificationEvent<PathItem>>{

		@Override
		public void handle(TreeModificationEvent<PathItem> arg0) {
			CheckBoxTreeItem<PathItem> source = (CheckBoxTreeItem<PathItem>) arg0.getSource();
			Path path = source.getValue().getPath();
			FileHelper file = new FileHelper(source.getValue().getPath(), source.getValue().isFile());
			if(source.isSelected() || source.isIndeterminate()){
				logger.trace("Add {} to SYNC", path);
				getToSynchronize().add(file);
				getToDesynchronize().remove(file);
			} else if(!source.isIndeterminate()){
				logger.trace("Remove {} from SYNC", path);
				getToSynchronize().remove(file);
				getToDesynchronize().add(file);

				ImageView view;
				if(file.isFile()){
					view = new ImageView(standardIcon);
				} else {
					view = new ImageView(standardFolderIcon);
				}
				
				updateIconInUIThread(source, view);
				updateTooltipInUIThread(source, softDeletedTooltip);
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
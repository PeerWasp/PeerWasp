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
	
	private Image inProgressIcon = new Image(getClass().getResourceAsStream("/images/file-synch.png"));
	private Image successIcon = new Image(getClass().getResourceAsStream("/images/file-success.png"));
	private Image errorIcon = new Image(getClass().getResourceAsStream("/images/file-error.png"));
	private Image standardIcon = new Image(getClass().getResourceAsStream("/images/file-standard.png"));

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
		PathTreeItem invisibleRoot = new PathTreeItem(userConfig.getRootPath());
		fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());
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
				ImageView view = null;
	        	Path path = topLevelNode.getFile().toPath();
	        	boolean isSynched = synchronizedFiles.contains(path);
	        	
	        	if(failedFiles.contains(path)){
	        		view = new ImageView(errorIcon);
	        	} else if(executingFiles.contains(path)){
	        		view = new ImageView(inProgressIcon);
	        	} else {
	        		view = new ImageView(successIcon);
	        	}
	        	
	        	putTreeItem(topLevelNode.getFile().toPath(), isSynched, view);
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
		PathTreeItem root = (PathTreeItem)fileTreeView.getRoot();
		root.setSelected(true);
	}

	@FXML
	public void unselectAllAction(ActionEvent event) {
		PathTreeItem root = (PathTreeItem)fileTreeView.getRoot();
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
		createTreeWithFilesFromNetwork();
	}


	@Override
	@Handler
	public void onExecutionStarts(FileExecutionStartedMessage message) {
		logger.trace("onExecutionStarts: {}", message.getPath());
		ImageView view = new ImageView(inProgressIcon);
		TreeItem<PathItem> item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath(), false, view);
			logger.trace("item == null for {}", message.getPath());
		}
		forceUpdateTreeItem(item);
		item.setGraphic(view);
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
		        	removeTreeItem(message.getPath());
		        }
			});
			break;
		default:
			ImageView view = new ImageView(successIcon);
			logger.trace("onExecutionSucceeds: {}", message.getPath());
			CheckBoxTreeItem<PathItem> item = getTreeItem(message.getPath());
			
			if(item != null){
				logger.trace("item != null for {}, change icon!", message.getPath());
			} else {
				item = putTreeItem(message.getPath(), true, view);
				logger.trace("item == null for {}", message.getPath());
			}
			
			item.setGraphic(view);
			forceUpdateTreeItem(item);
			final CheckBoxTreeItem<PathItem> item2 = item;
			javafx.application.Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
		        	if(message.getPath().toFile().isDirectory()){
		        		item2.setIndeterminate(true);
		        	} else {
		        		item2.setSelected(true);
		        	}
		        	
		        }
			});
		}
	}

	@Override
	@Handler
	public void onExecutionFails(FileExecutionFailedMessage message) {
		ImageView view = new ImageView(errorIcon);
		logger.trace("onExecutionFails: {}", message.getPath());
		PathTreeItem item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath(), false, view);
			logger.trace("item == null for {}", message.getPath());
		}
		forceUpdateTreeItem(item);
		item.setGraphic(view);
		item.setSelected(true);
	}
	
	@Override
	@Handler
	public void onFileSoftDeleted(LocalFileDesyncMessage message) {
		logger.trace("onFileSoftDeleted: {}", message.getPath());
		Path path = message.getPath();
		ImageView view = new ImageView(standardIcon);
		PathTreeItem item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath(), false, view);
			logger.trace("item == null for {}", message.getPath());
		}
		
		item.setGraphic(view);
		forceUpdateTreeItem(item);
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
	
	private PathTreeItem getTreeItem(Path path){
		PathTreeItem root = (PathTreeItem)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		return findTreeItem(root, path, false);
	}

	private PathTreeItem putTreeItem(Path path, boolean isSynched, ImageView view){
		PathTreeItem root = (PathTreeItem)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		Path pathLeft = prefix.relativize(path);
		return putTreeItem(root, pathLeft, isSynched, view);
	}
	private PathTreeItem removeTreeItem(Path path){
		PathTreeItem root = (PathTreeItem)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		return findTreeItem(root, path, true);
		
	}
	private PathTreeItem findTreeItem(PathTreeItem item, Path path, boolean remove){
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
			PathTreeItem castedChild = (PathTreeItem)child;
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
	
	private PathTreeItem putTreeItem(PathTreeItem parent, Path pathLeft, boolean isSynched, ImageView view){
		Path parentPath = parent.getValue().getPath();
		Path wholePath = parentPath.resolve(pathLeft);

		if(pathLeft.getNameCount() == 1){
			PathTreeItem created = new PathTreeItem(wholePath, view, isSynched);
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
				PathTreeItem child = (PathTreeItem)iter.next();
				if(child.getValue().getPath().equals(pathToSearch)){
					return putTreeItem(child, child.getValue().getPath().relativize(wholePath), isSynched, view);
				}
			}
			PathTreeItem created = new PathTreeItem(pathToSearch, view, isSynched);
			created.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler());
			parent.getChildren().add(created);
			return putTreeItem(created, created.getValue().getPath().relativize(wholePath), isSynched, view);
		}
	}

	private class ClickEventHandler implements EventHandler<TreeModificationEvent<PathItem>>{

		@Override
		public void handle(TreeModificationEvent<PathItem> arg0) {
			PathTreeItem source = (PathTreeItem) arg0.getSource();
			Path path = source.getValue().getPath();
			FileHelper file = new FileHelper(source.getValue().getPath(), source.isFile());
			if(source.isSelected() || source.isIndeterminate()){
				logger.trace("Add {} to SYNC", path);
				getToSynchronize().add(file);
				getToDesynchronize().remove(file);
			} else if(!source.isIndeterminate()){
				logger.trace("Remove {} from SYNC", path);
				getToSynchronize().remove(file);
				getToDesynchronize().add(file);
				source.setGraphic(new ImageView(standardIcon));
				forceUpdateTreeItem(source);
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
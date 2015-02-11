package org.peerbox.presenter.settings.synchronization;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.presenter.settings.synchronization.eventbus.IExecutionMessageListener;
import org.peerbox.presenter.settings.synchronization.messages.ExecutionStartsMessage;
import org.peerbox.presenter.settings.synchronization.messages.ExecutionSuccessfulMessage;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class Synchronization implements Initializable, IExecutionMessageListener{

	private static final Logger logger = LoggerFactory.getLogger(Synchronization.class);
	private Set<Path> synchronizedFiles;

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
	
	public Set<FileHelper> getToSynchronize(){
		return toSynchronize;
	}

	public Set<FileHelper> getToDesynchronize(){
		return toDesynchronize;
	}

	private Vector<FileComponent> toSync = new Vector<FileComponent>();

	@Inject
	public Synchronization(IFileManager fileManager, FileEventManager eventManager, UserConfig userConfig) {
		this.eventManager = eventManager;
		this.fileManager = fileManager;
		this.userConfig = userConfig;
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
			listFiles(filesFromNetwork);
		} catch (NoSessionException | NoPeerConnectionException
				| InvalidProcessStateException | ProcessExecutionException e) {
			e.printStackTrace();
		}
	}

	private void listFiles(FileNode fileNode){
		ImageView graphic = new ImageView(new Image(getClass().getResourceAsStream("/images/folder.jpg"))); 
		PathTreeItem invisibleRoot = new PathTreeItem(userConfig.getRootPath());
		//new CheckBoxTreeItem<PathItem>(new PathItem(userConfig.getRootPath()));
		fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());
	    fileTreeView.setRoot(invisibleRoot);
        fileTreeView.setEditable(false);
        fileTreeView.setShowRoot(false);
//
        createTreeFromFileNode(fileNode);

	}
	
	private void createTreeFromFileNode(FileNode fileNode){
        for(FileNode topLevelNode : fileNode.getChildren()){
        	boolean isSynched = synchronizedFiles.contains(topLevelNode.getFile().toPath());
        	PathTreeItem item = putTreeItem(topLevelNode.getFile().toPath(), isSynched);
			createTreeFromFileNode(topLevelNode);
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
	public void onExecutionStarts(ExecutionStartsMessage message) {
		logger.trace("onExecutionStarts: {}", message.getPath());
		TreeItem<PathItem> item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath(), false);
			logger.trace("item == null for {}", message.getPath());
		}
		forceUpdateTreeItem(item);
		item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/index_progress.jpg"))));

	}

	@Override
	@Handler
	public void onExecutionSucceeds(ExecutionSuccessfulMessage message) {
		logger.trace("onExecutionSucceeds: {}", message.getPath());
		CheckBoxTreeItem<PathItem> item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath(), true);
			logger.trace("item == null for {}", message.getPath());
		}
		forceUpdateTreeItem(item);
		item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/index_successful.jpg"))));
//		item.setSelected(true);
	}

	@Override
	@Handler
	public void onExecutionFails(FileExecutionFailedMessage message) {
		logger.trace("onExecutionFails: {}", message.getPath());
		PathTreeItem item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath(), false);
			logger.trace("item == null for {}", message.getPath());
		}
		forceUpdateTreeItem(item);
		item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/index_failed.jpg"))));
		item.setSelected(true);
	}
	
	private PathTreeItem getTreeItem(Path path){
		PathTreeItem root = (PathTreeItem)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		return getTreeItem(root, path);
		
	}
	
	private void forceUpdateTreeItem(TreeItem<PathItem> item){
		PathItem value = item.getValue();
		item.setValue(null);
		item.setValue(value);
	}
	
	private PathTreeItem getTreeItem(PathTreeItem item, Path path){
		Path wholePath = path;
		Path prefix = item.getValue().getPath();
		if(path.startsWith(prefix)){
			path = prefix.relativize(wholePath);
		}
		if(path.equals(Paths.get(""))){
			logger.trace("Successful! {}", item.getValue().getPath());
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
				 return getTreeItem(castedChild, wholePath);
			 }
		}
		return null;
	}
	
	private PathTreeItem putTreeItem(Path path, boolean isSynched){
		PathTreeItem root = (PathTreeItem)fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		Path pathLeft = prefix.relativize(path);
		return putTreeItem(root, pathLeft, isSynched);
	}
	
	private PathTreeItem putTreeItem(PathTreeItem parent, Path pathLeft, boolean isSynched){
		Path parentPath = parent.getValue().getPath();
		Path wholePath = parentPath.resolve(pathLeft);

		if(pathLeft.getNameCount() == 1){
			PathTreeItem created = new PathTreeItem(wholePath);
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
					return putTreeItem(child, child.getValue().getPath().relativize(wholePath), isSynched);
				}
			}
			PathTreeItem created = new PathTreeItem(pathToSearch);
			created.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), new ClickEventHandler());
			created.setSelected(isSynched);
			parent.getChildren().add(created);
			return putTreeItem(created, created.getValue().getPath().relativize(wholePath), isSynched);
		}
	}

//	private void showDummyData(){
//		FileNode root = new FileNode(null, userConfig.getRootPath().toFile(), userConfig.getRootPath().toString(), null, null);
//        PathTreeItem rootItem =
//            new PathTreeItem(root, this, false);
//        rootItem.setExpanded(true);
//
//        fileTreeView.setEditable(false);
//
//        fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());
//
//        Path rootFolder = userConfig.getRootPath();
//
//        try(Stream<Path> dirs = Files.list(rootFolder)){
//        	Iterator<Path> iter = dirs.iterator();
//        	while(iter.hasNext()){
//        		Path folder = iter.next();
//        		FileNode item = new FileNode(null, folder.toFile(), userConfig.getRootPath().toString(), null, null);
//        		PathTreeItem checkBoxTreeItem = new PathTreeItem(item, this, false);
//        		rootItem.getChildren().add(checkBoxTreeItem);
//        	}
//
//        } catch (IOException ex){
//        	ex.printStackTrace();
//        }
//
//
//        fileTreeView.setRoot(rootItem);
//        fileTreeView.setShowRoot(true);
//	}
	
	private class ClickEventHandler implements EventHandler<TreeModificationEvent<PathItem>>{

		@Override
		public void handle(TreeModificationEvent<PathItem> arg0) {
				PathTreeItem source = (PathTreeItem) arg0.getSource();
				Path path = source.getValue().getPath();
//				if(!source.getIsRoot()){
				FileHelper file = new FileHelper(source.getValue().getPath(), source.isFile());
					if(source.isSelected() || source.isIndeterminate()){
						getToSynchronize().add(file);
						getToDesynchronize().remove(file);
					} else if(!source.isIndeterminate()){
						getToSynchronize().remove(file);
						getToDesynchronize().add(file);
					}
//				}
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



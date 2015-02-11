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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
	private TreeSet<FileNode> toSynchronize = new TreeSet<FileNode>(new FileNodeComparator());
	private TreeSet<FileNode> toDesynchronize = new TreeSet<FileNode>(new FileNodeComparator());
	
	private final ImageView successIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/index_successful.jpg")));
	private final ImageView failureIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/index_failed.jpg")));
	private final ImageView progressIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/index_progress.jpg")));
	private final ImageView defaultIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/index.jpg")));
	
	//	private ExecutionMessageListener messageListener;

	public Set<FileNode> getToSynchronize(){
		return toSynchronize;
	}

	public Set<FileNode> getToDesynchronize(){
		return toDesynchronize;
	}

	private Vector<FileComponent> toSync = new Vector<FileComponent>();

	@Inject
	public Synchronization(IFileManager fileManager, FileEventManager eventManager) {
		this.eventManager = eventManager;
		this.fileManager = fileManager;
//		this.messageListener = messageListener;
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
		PathTreeItem invisibleRoot = new PathTreeItem(fileNode, this, graphic, false, true);
		fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());
	    fileTreeView.setRoot(invisibleRoot);
        fileTreeView.setEditable(false);
        fileTreeView.setShowRoot(false);
//
//        for(FileNode topLevelNode : fileNode.getChildren()){
//        	boolean isSynched = synchronizedFiles.contains(topLevelNode.getFile().toPath());
//			PathTreeItem rootItem = new PathTreeItem(topLevelNode, this, graphic, isSynched);
//			invisibleRoot.getChildren().add(rootItem);
//		}
	}

	public void acceptSyncAction(ActionEvent event) {
		synchronizedFiles = eventManager.getFileTree().getSynchronizedPathsAsSet();

		for(FileNode node : toSynchronize){
			if(!synchronizedFiles.contains(node.getFile().toPath()))
				eventManager.onFileSynchronized(node.getFile().toPath(), node.isFolder());
		}
		for(FileNode node: toDesynchronize.descendingSet()){
			eventManager.onFileDesynchronized(node.getFile().toPath());
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
	
	private class FileNodeComparator implements Comparator<FileNode>{
		
		@Override
		public int compare(FileNode o1, FileNode o2) {
			String path1 = o1.getPath().toString();
			String path2 = o2.getPath().toString();
			return path1.compareTo(path2);
		}
	}


	@Override
	@Handler
	public void onExecutionStarts(ExecutionStartsMessage message) {
		logger.trace("onExecutionStarts: {}", message.getPath());
		TreeItem<PathItem> item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath());
			logger.trace("item == null for {}", message.getPath());
		}

		item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/index_progress.jpg"))));
		forceUpdateTreeItem(item);
	}

	@Override
	@Handler
	public void onExecutionSucceeds(ExecutionSuccessfulMessage message) {
		logger.trace("onExecutionSucceeds: {}", message.getPath());
		TreeItem<PathItem> item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath());
			logger.trace("item == null for {}", message.getPath());
		}
		item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/index_successful.jpg"))));
		forceUpdateTreeItem(item);
	}

	@Override
	@Handler
	public void onExecutionFails(FileExecutionFailedMessage message) {
		logger.trace("onExecutionFails: {}", message.getPath());
		TreeItem<PathItem> item = getTreeItem(message.getPath());
		if(item != null){
			logger.trace("item != null for {}, change icon!", message.getPath());
		} else {
			item = putTreeItem(message.getPath());
			logger.trace("item == null for {}", message.getPath());
		}

		item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/index_failed.jpg"))));
		forceUpdateTreeItem(item);
	}
	
	private TreeItem<PathItem> getTreeItem(Path path){
		TreeItem<PathItem> root = fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		return getTreeItem(root, path);
		
	}
	
	private void forceUpdateTreeItem(TreeItem<PathItem> item){
		PathItem value = item.getValue();
		item.setValue(null);
		item.setValue(value);
	}
	
	private TreeItem<PathItem> getTreeItem(TreeItem<PathItem> item, Path path){
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
			logger.trace("check child {} of {}", child.getValue().getPath(), item.getValue().getPath());
			
			Path childNextLevel = prefix.relativize(child.getValue().getPath()).getName(0);
			logger.trace("childNextLevel: {} nextLevel {}", childNextLevel, nextLevel);
			if(childNextLevel.equals(nextLevel)){
				 logger.trace("Found as next level {} for {}", item.getValue().getPath(), wholePath);
				 return getTreeItem(child, wholePath);
			 }
		}
		return null;
	}
	
	private TreeItem<PathItem> putTreeItem(Path path){
		TreeItem<PathItem> root = fileTreeView.getRoot();
		Path prefix = root.getValue().getPath();
		Path pathLeft = prefix.relativize(path);
		return putTreeItem(root, pathLeft);
	}
	
	private TreeItem<PathItem> putTreeItem(TreeItem<PathItem> parent, Path pathLeft){
		Path parentPath = parent.getValue().getPath();
		Path wholePath = parentPath.resolve(pathLeft);

		if(pathLeft.getNameCount() == 1){
			TreeItem<PathItem> created = new TreeItem<PathItem>(new PathItem(wholePath));
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
				TreeItem<PathItem> child = iter.next();
				if(child.getValue().getPath().equals(pathToSearch)){
					return putTreeItem(child, child.getValue().getPath().relativize(wholePath));
				}
			}
			TreeItem<PathItem> created = new TreeItem<PathItem>(new PathItem(pathToSearch));
			parent.getChildren().add(created);
			return putTreeItem(created, created.getValue().getPath().relativize(wholePath));
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
}

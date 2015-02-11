package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.controlsfx.control.PropertySheet.Item;
import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PathTreeItem extends CheckBoxTreeItem<PathItem> {
	private static final Logger logger = LoggerFactory.getLogger(PathTreeItem.class);
    private boolean isLeaf = false;
    private boolean isRoot = false;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeft = true;
    private FileNode fileNode;
    private IFileEventManager fileEventManager;
    private Synchronization sync;
    private ImageView graphic;//  = new ImageView(new Image(PathTreeItem.class.getResourceAsStream("../folder.jpg"))); 

    public PathTreeItem(FileNode file, Synchronization sync, ImageView graphic, boolean isSelected, boolean isRoot){
    	super(new PathItem(file.getFile().toPath()), graphic, isSelected);
      	this.fileEventManager = sync.getFileEventManager();
      	this.fileNode = file;
      	this.sync = sync;
      	this.isRoot = isRoot;
      	this.graphic = graphic;

      	if(!isRoot){
          	this.setSelected(isSelected);
          	addEventHandler(PathTreeItem.checkBoxSelectionChangedEvent(), new EventHandler<TreeModificationEvent<PathItem>>(){
          		@Override
        		public void handle(
        				javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent<PathItem> arg0) {
        			PathTreeItem pathItem = (PathTreeItem) arg0.getSource();
        			Path path = pathItem.getValue().getPath();
        			PathTreeItem source = (PathTreeItem)arg0.getSource();
        			if(!source.getIsRoot()){
        				if(source.isSelected() || source.isIndeterminate()){
        					sync.getToSynchronize().add(source.getFileNode());
        					sync.getToDesynchronize().remove(source.getFileNode());
        				} else if(!source.isIndeterminate()){
        					sync.getToSynchronize().remove(source.getFileNode());
        					sync.getToDesynchronize().add(source.getFileNode());
        				}
        			}
        			arg0.consume();
        		}
          	});
      	}
    }
    
    public PathTreeItem(FileNode file, Synchronization sync, ImageView graphic, boolean isSelected) {
    	this(file, sync, graphic, isSelected, false);
    }
    
    public boolean getIsRoot(){
    	return isRoot;
    }

    public static PathTreeItem createNode(FileNode fileNode, Synchronization sync, boolean isSelected) {
    	ImageView graphic = new ImageView(new Image(PathTreeItem.class.getResourceAsStream("/images/folder.jpg"))); 
        return new PathTreeItem(fileNode, sync, graphic, isSelected, false);
    }

    public FileNode getFileNode(){
    	return fileNode;
    }
    
    @Override
    public ObservableList<TreeItem<PathItem>> getChildren() {
        if (isFirstTimeChildren && !isRoot) {
            isFirstTimeChildren = false;
//            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }  

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeft) {
            isFirstTimeLeft = false;
            Path path = getValue().getPath();
            isLeaf = !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
        }
        return isLeaf;
    }

    private ObservableList<TreeItem<PathItem>> buildChildren(TreeItem<PathItem> treeItem) {
        Path path = treeItem.getValue().getPath();
        if (path != null && fileNode.isFolder()) {
            ObservableList<TreeItem<PathItem>> children = FXCollections.observableArrayList();
        	List<FileNode> fileNodes = fileNode.getChildren();
        	
        	Set<Path> synchronizedFiles = fileEventManager.getFileTree().getSynchronizedPathsAsSet();
            for (FileNode node : fileNodes) {
            	if(synchronizedFiles.contains(node.getFile().toPath())){
            		children.add(createNode(node, sync, true));
            	} else {
            		children.add(createNode(node, sync, false));
            	}
            }
            return children;
        }
        return FXCollections.emptyObservableList();
    }
}
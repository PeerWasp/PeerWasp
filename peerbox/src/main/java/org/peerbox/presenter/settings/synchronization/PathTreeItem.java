package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.watchservice.IFileEventManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends CheckBoxTreeItem<PathItem> {
    private boolean isLeaf = false;
    private boolean isRoot = false;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeft = true;
    private FileNode fileNode;
    private IFileEventManager fileEventManager;
    private Synchronization sync;

    public PathTreeItem(FileNode file, Synchronization sync, boolean isSelected, boolean isRoot){
    	super(new PathItem(file.getFile().toPath()), null, isSelected);
      	this.fileEventManager = sync.getFileEventManager(); //fileEventManager;
      	this.fileNode = file;
      	this.sync = sync;
      	this.isRoot = isRoot;

      	if(isRoot){
//      		addEventHandler(PathTreeItem.checkBoxSelectionChangedEvent(), new RootSelectionEventHandler());
      	} else {
          	this.setSelected(isSelected);
          	addEventHandler(PathTreeItem.checkBoxSelectionChangedEvent(), new DefaultSelectionEventHandler());
      	}
    }
    
    public PathTreeItem(FileNode file, Synchronization sync, boolean isSelected) {
    	this(file, sync, isSelected, false);
    }
    
    public boolean getIsRoot(){
    	return isRoot;
    }

      

    public static PathTreeItem createNode(FileNode fileNode, Synchronization sync, boolean isSelected) {
        return new PathTreeItem(fileNode, sync, isSelected, false);
    }

    public FileNode getFileNode(){
    	return fileNode;
    }
    
    @Override
    public ObservableList<TreeItem<PathItem>> getChildren() {
        if (isFirstTimeChildren && !isRoot) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
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
        	for(Path syncPath: synchronizedFiles){
        		System.out.println("SYNCED file: " +  syncPath);
        	}
            for (FileNode node : fileNodes) {
            	if(synchronizedFiles.contains(node.getFile().toPath())){
            		System.out.println("Contains " + node.getFile().toPath());
            		children.add(createNode(node, sync, true));
            	} else {
            		System.out.println("Not contains " + node.getFile().toPath());
            		children.add(createNode(node, sync, false));
            	}
            }
            return children;
        }
        return FXCollections.emptyObservableList();
    }
    
    private class DefaultSelectionEventHandler implements EventHandler<TreeModificationEvent<PathItem>>{

    	@Override
		public void handle(
				javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent<PathItem> arg0) {
			PathTreeItem pathItem = (PathTreeItem) arg0.getSource();
			Path path = pathItem.getValue().getPath();
			System.out.println("Catched Event: " + path);
			PathTreeItem source = (PathTreeItem)arg0.getSource();
			if(!source.isIndeterminate() && !source.getIsRoot()){
				if(source.isSelected()){
					sync.getToSynchronize().add(source.getFileNode());
					sync.getToDesynchronize().remove(source.getFileNode());
				} else if(!source.isIndeterminate()){
					sync.getToSynchronize().remove(source.getFileNode());
					sync.getToDesynchronize().add(source.getFileNode());
				}
			}
			arg0.consume();
		}
    }
    
//    private class RootSelectionEventHandler implements EventHandler<TreeModificationEvent<PathItem>>{
//
//		@Override
//		public void handle(
//				javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent<PathItem> arg0) {
//			// TODO Auto-generated method stub
//			PathTreeItem pathItem = (PathTreeItem) arg0.getSource();
//			pathItem.setIndeterminate(true);
//			pathItem.setSelected(false);
//			
//			Alert alert = new Alert(AlertType.WARNING);
//			alert.setTitle("Attempt to desynchronize all files");
//			alert.setHeaderText("You cannot desynchronize the root folder.");
//			alert.setContentText("If this was your intention, please unselect all files and folders"
//					+ "in your PeerBox root folder.");
//			alert.showAndWait();
//		}
//    	
//    }
}
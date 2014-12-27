package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.watchservice.IFileEventManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends CheckBoxTreeItem<PathItem> {
    private boolean isLeaf = false;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeft = true;
    private FileNode fileNode;
    private IFileEventManager fileEventManager;
    private Synchronization sync;

    public PathTreeItem(FileNode file, Synchronization sync, boolean isSelected) {

        super(new PathItem(file.getFile().toPath()));
    	this.fileEventManager = sync.getFileEventManager(); //fileEventManager;
    	this.fileNode = file;
    	this.sync = sync;
    	this.setSelected(isSelected);
    	
    	addEventHandler(PathTreeItem.checkBoxSelectionChangedEvent(), new EventHandler<TreeModificationEvent<PathItem>>() {

			@Override
			public void handle(
					javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent<PathItem> arg0) {
				PathTreeItem pathItem = (PathTreeItem) arg0.getSource();
				Path path = pathItem.getValue().getPath();
				System.out.println("Catched Event: " + path);
				PathTreeItem source = (PathTreeItem)arg0.getSource();
				if(!source.isIndeterminate()){
					if(source.isSelected()){
						sync.getToSynchronize().add(source.getFileNode());
						sync.getToDesynchronize().remove(source.getFileNode());
					} else {
						sync.getToSynchronize().remove(source.getFileNode());
						sync.getToDesynchronize().add(source.getFileNode());
					}
				}
				arg0.consume();
				
			}

		});
    }

    public static PathTreeItem createNode(FileNode fileNode, Synchronization sync, boolean isSelected) {
        return new PathTreeItem(fileNode, sync, isSelected);
    }

    public FileNode getFileNode(){
    	return fileNode;
    }
    
    @Override
    public ObservableList<TreeItem<PathItem>> getChildren() {
        if (isFirstTimeChildren) {
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
            for (FileNode node : fileNodes) {
            	if(fileEventManager.getSynchronizedFiles().contains(node.getFile().toPath())){
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
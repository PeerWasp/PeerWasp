package org.peerbox.presenter.settings.synchronization;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FileEventManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends CheckBoxTreeItem<PathItem> {
    private boolean isLeaf = false;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeft = true;
    private FileNode fileNode;
    private Path path;
    private FileEventManager fileEventManager;
    private Synchronization sync;

    public PathTreeItem(FileNode file, Synchronization sync, boolean isSelected) {

        super(new PathItem(file.getFile().toPath()));
    	this.path = file.getFile().toPath();
    	this.fileEventManager = sync.getFileEventManager(); //fileEventManager;
    	this.fileNode = file;
    	this.sync = sync;
    	this.setSelected(isSelected);
    	
    	addEventHandler(PathTreeItem.checkBoxSelectionChangedEvent(), new EventHandler<TreeModificationEvent<PathItem>>() {

			@Override
			public void handle(
					javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent<PathItem> arg0) {
				// TODO Auto-generated method stub
				System.out.println("Catched Event!");
				PathTreeItem source = (PathTreeItem)arg0.getSource();
				if(source.isSelected()){
					sync.getToSynchronize().add(source.getValue().getPath());
					sync.getToDesynchronize().remove(source.getValue().getPath());
				} else {
					sync.getToSynchronize().remove(source.getValue().getPath());
					sync.getToDesynchronize().add(source.getValue().getPath());
				}
				
			}

		});
    }

    public static PathTreeItem createNode(FileNode fileNode, Synchronization sync, boolean isSelected) {
        return new PathTreeItem(fileNode, sync, isSelected);
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
        if (path != null && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            ObservableList<TreeItem<PathItem>> children = FXCollections.observableArrayList();
           // try (DirectoryStream<Path> dirs = Files.newDirectoryStream(path)) {
            	List<FileNode> fileNodes = fileNode.getChildren();
                for (FileNode node : fileNodes) {
                    //path pathItem = new PathItem(dir);
                	
                	FileComponent comp = fileEventManager.getFileTree().getComponent(node.getPath());
                	if(comp != null && comp.getIsSynchronized()){
                		 children.add(createNode(node, sync, true));
                	} else {
                		children.add(createNode(node, sync, false));
                	}
                    
                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
            return children;
        }
        return FXCollections.emptyObservableList();
    }
}
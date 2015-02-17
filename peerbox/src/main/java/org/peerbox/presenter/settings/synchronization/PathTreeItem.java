package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeCell;

import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PathTreeItem extends CheckBoxTreeItem<PathItem> {
	
	private static final Logger logger = LoggerFactory.getLogger(PathTreeItem.class);
	private boolean isFile;
	
    private IFileEventManager fileEventManager;

    public PathTreeItem(Path path){
    	this(path, null, false);
    }
    
    public PathTreeItem(Path path, ImageView view){
    	this(path, view, false);
    }
    
    public PathTreeItem(Path path, ImageView view, boolean isSynched){
    	this(path, view, isSynched, true);
    }
    
    public PathTreeItem(Path path, ImageView view, boolean isSynched, boolean isFile){
    	super(new PathItem(path));
    	
        Label label = new Label();
//        label.setGraphic(view);
//        final Tooltip tooltip = new Tooltip("Uncheck to remove the file\n from selective synchronization.");
//        label.setTooltip(tooltip);
//    	setGraphic(label);
        setGraphic(view);
    	setSelected(isSynched);
    	setIsFile(isFile);
    	
//    	MenuItem deleteItem = new MenuItem("Delete from network");
//        menu.getItems().add(deleteItem);
//        deleteItem.setOnAction(new EventHandler() {
//            public void handle(Event t) {
//            	PathTreeItem source = (PathTreeItem)t.getSource();
//            	Path path = source.getValue().getPath();
//                fileEventManager.onLocalFileHardDelete(path);
//                logger.trace("Initiate hard delete for file {} from GUI", path);
//            }
//        });
//        setContextMenu(menu);
    }
    
    
    public PathTreeItem(Path path, boolean isSelected, boolean isFile){
    	super(new PathItem(path));
      	this.isFile = isFile;
      	setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/index_progress.jpg"))));
    }
    
    public boolean isFile(){
    	return isFile;
    }
    
    private void setIsFile(boolean isFile){
    	this.isFile = isFile;
    }
    
    public boolean isFolder(){
    	return !isFile;
    }
}
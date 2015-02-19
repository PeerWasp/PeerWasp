//package org.peerbox.presenter.settings.synchronization;
//
//import java.nio.file.Path;
//
//import javafx.event.Event;
//import javafx.event.EventHandler;
//import javafx.scene.control.CheckBoxTreeItem;
//import javafx.scene.control.Label;
//import javafx.scene.control.Tooltip;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.control.ContextMenu;
//import javafx.scene.control.MenuItem;
//import javafx.scene.control.TreeItem;
//import javafx.scene.control.TreeCell;
//
//import org.peerbox.watchservice.IFileEventManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//
//public class PathTreeItem extends CheckBoxTreeItem<PathItem> {
//	
//	private static final Logger logger = LoggerFactory.getLogger(PathTreeItem.class);
//
//	
//    private IFileEventManager fileEventManager;
//
//    public PathTreeItem(Path path){
//    	this(path, null, false);
//    }
//    
//    public PathTreeItem(Path path, ImageView view){
//    	this(path, view, false);
//    }
//    
//    public PathTreeItem(Path path, ImageView view, boolean isSynched){
//    	this(path, view, isSynched, true);
//    }
//    
//    public PathTreeItem(Path path, ImageView view, boolean isSynched, boolean isFile){
//    	super(new PathItem(path));
//    	setSelected(isSynched);
//    	setIsFile(isFile);
//    	Label label = new Label(path.toString());
//        label.setGraphic(view);
//    	javafx.application.Platform.runLater(new Runnable() {
//	        @Override
//	        public void run() {
//
//        final Tooltip tooltip;
//	         tooltip = new Tooltip("Uncheck to remove the file\n from selective synchronization.");
//	         label.setTooltip(tooltip);
//	        }
//		});
//        setGraphic(label);
//    }
//}
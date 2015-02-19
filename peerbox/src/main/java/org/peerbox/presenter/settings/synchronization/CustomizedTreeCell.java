package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;


import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.CheckBoxTreeItem;
public class CustomizedTreeCell extends CheckBoxTreeCell<PathItem> {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomizedTreeCell.class);
	private ContextMenu menu = new ContextMenu();
	private CheckBoxTreeItem<PathItem> item;
	
	public CustomizedTreeCell(IFileEventManager fileEventManager){
		
		MenuItem deleteItem = new MenuItem("Delete from network");
		menu.getItems().add(deleteItem);
		deleteItem.setOnAction(new EventHandler() {
			public void handle(Event t) {
				fileEventManager.onLocalFileHardDelete(getItem().getPath());
			}
		});
		
//		final Tooltip tooltip = new Tooltip();
//        tooltip.setText(
//            "\nThis is an example tooltip\n");
	  
        setContextMenu(menu);
//        setTooltip(tooltip);
	}

}

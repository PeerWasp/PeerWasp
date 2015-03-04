package org.peerbox.presenter.settings.synchronization;

import java.io.IOException;

import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.share.IShareFolderHandler;
import org.peerbox.share.ShareFolderController;
import org.peerbox.share.ShareFolderHandler;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Provider;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.util.Callback;

/**
 * This class is used as a template to produce the context menu of
 * the {@link javafx.scene.control.CheckBoxTreeItem CheckBoxTreeItem}s 
 * for the {@link org.peerbox.presenter.settings.synchronization.
 * Synchronization Synchronization} class. 
 * @author Claudio
 *
 */
public class CustomizedTreeCell extends CheckBoxTreeCell<PathItem> {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomizedTreeCell.class);
	private ContextMenu menu = new ContextMenu();
	
	public CustomizedTreeCell(IFileEventManager fileEventManager, 
			Provider<IShareFolderHandler> shareFolderHandlerProvider){
		
		MenuItem deleteItem = new MenuItem("Delete from network");
		MenuItem shareItem = new MenuItem("Share");

		menu.getItems().add(deleteItem);
		menu.getItems().add(shareItem);
			
		menu.setOnShowing(new EventHandler() {
			@Override
			public void handle(Event arg0) {
				if(getItem().isFile()){
					shareItem.setVisible(false);
				} else if(!getItem().getPath().toFile().exists()){
					shareItem.setDisable(true);
				} else {
					shareItem.setDisable(false);
					shareItem.setVisible(true);
				}
			}
		});
	
		deleteItem.setOnAction(new EventHandler() {
			public void handle(Event t) {
				fileEventManager.onLocalFileHardDelete(getItem().getPath());
			}
		});
		
        setContextMenu(menu);
	}

}

package org.peerbox.presenter.settings.synchronization;




import java.awt.MenuItem;
import java.io.IOException;
import java.nio.file.Path;

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

import javafx.scene.shape.Rectangle;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.stage.WindowEvent;
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
	private Provider<IShareFolderHandler> shareFolderHandlerProvider;

	public CustomizedTreeCell(IFileEventManager fileEventManager,
			Provider<IShareFolderHandler> shareFolderHandlerProvider){

		this.shareFolderHandlerProvider = shareFolderHandlerProvider;

		CustomMenuItem deleteItem = new CustomMenuItem(new Label("Delete from network"));
		Label shareLabel = new Label("Share");
		shareLabel.setTooltip(new Tooltip("haha"));
		CustomMenuItem shareItem = new CustomMenuItem(shareLabel);

		shareItem.setOnAction(new ShareFolderAction());

		menu.getItems().add(deleteItem);
		menu.getItems().add(shareItem);

		menu.setOnShowing(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				if(getItem().isFile()){
					shareItem.setVisible(false);
				} else if(!getItem().getPath().toFile().exists()){
					shareItem.setDisable(true);
					Label label = (Label)shareItem.getContent();
					label.setTooltip(new Tooltip("You cannot share this folder as it is not synchronized."));
				} else {
					shareItem.setDisable(false);
					shareItem.setVisible(true);
					Rectangle rect = new Rectangle();
					rect.setWidth(200);
					rect.setHeight(100);

					Label label = (Label)shareItem.getContent();
					label.setTooltip(new Tooltip("Right-click to share this folder with a friend."));
				}
			}
		});

		deleteItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				fileEventManager.onLocalFileHardDelete(getItem().getPath());
			}
		});

        setContextMenu(menu);
	}


	private class ShareFolderAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			IShareFolderHandler handler = shareFolderHandlerProvider.get();
			Path toShare = getItem().getPath();
			handler.shareFolder(toShare);
		}
	}

}

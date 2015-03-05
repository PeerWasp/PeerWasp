package org.peerbox.presenter.settings.synchronization;




import java.io.IOException;
import java.nio.file.Path;

import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.filerecovery.IFileRecoveryHandler;
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
import javafx.scene.control.MenuItem;
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

	private Provider<IFileRecoveryHandler> recoverFileHandlerProvider;
	private Provider<IShareFolderHandler> shareFolderHandlerProvider;

	private MenuItem recoverMenuItem;

	public CustomizedTreeCell(IFileEventManager fileEventManager,
			Provider<IFileRecoveryHandler> recoverFileHandlerProvider,
			Provider<IShareFolderHandler> shareFolderHandlerProvider){

		this.shareFolderHandlerProvider = shareFolderHandlerProvider;
		this.recoverFileHandlerProvider = recoverFileHandlerProvider;

		CustomMenuItem deleteItem = new CustomMenuItem(new Label("Delete from network"));
		Label shareLabel = new Label("Share");
		shareLabel.setTooltip(new Tooltip("haha"));
		CustomMenuItem shareItem = new CustomMenuItem(shareLabel);

		shareItem.setOnAction(new ShareFolderAction());

		recoverMenuItem = createRecoveMenuItem();

		menu.getItems().add(deleteItem);
		menu.getItems().add(recoverMenuItem);
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


				if (getItem() != null && getItem().isFile()) {
					recoverMenuItem.setDisable(false);
				} else {
					recoverMenuItem.setDisable(true);
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

	private MenuItem createRecoveMenuItem() {
		Label label = new Label("Recover File");
		MenuItem menuItem = new CustomMenuItem(label);
		menuItem.setOnAction(new RecoverFileAction());
		return menuItem;
	}

	private class RecoverFileAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (getItem() != null && getItem().getPath() != null) {
				IFileRecoveryHandler handler = recoverFileHandlerProvider.get();
				Path toRecover = getItem().getPath();
				handler.recoverFile(toRecover);
			}
		}
	}

	private class ShareFolderAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (getItem() != null && getItem().getPath() != null) {
				IShareFolderHandler handler = shareFolderHandlerProvider.get();
				Path toShare = getItem().getPath();
				handler.shareFolder(toShare);
			}
		}
	}

}

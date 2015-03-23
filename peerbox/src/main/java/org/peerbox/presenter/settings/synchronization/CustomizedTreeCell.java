package org.peerbox.presenter.settings.synchronization;




import java.io.IOException;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.forcesync.IForceSyncHandler;
import org.peerbox.share.IShareFolderHandler;
import org.peerbox.utils.DialogUtils;
import org.peerbox.view.ViewNames;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

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

	private ContextMenu menu;

	private IFileEventManager fileEventManager;
	private Provider<IFileRecoveryHandler> recoverFileHandlerProvider;
	private Provider<IShareFolderHandler> shareFolderHandlerProvider;
	private Provider<IForceSyncHandler> forceSyncHandlerProvider;


	private Stage stage;


	private MenuItem recoverMenuItem;
	private CustomMenuItem deleteItem;
	private CustomMenuItem shareItem;
	private MenuItem propertiesItem;
	private MenuItem forceSyncItem;


	public CustomizedTreeCell(IFileEventManager fileEventManager,
			Provider<IFileRecoveryHandler> recoverFileHandlerProvider,
			Provider<IShareFolderHandler> shareFolderHandlerProvider,
			Provider<IForceSyncHandler> forceSyncHandlerProvider){

		this.fileEventManager = fileEventManager;
		this.shareFolderHandlerProvider = shareFolderHandlerProvider;
		this.recoverFileHandlerProvider = recoverFileHandlerProvider;
		this.forceSyncHandlerProvider = forceSyncHandlerProvider;

		menu = new ContextMenu();

		deleteItem = new CustomMenuItem(new Label("Delete from network"));
		deleteItem.setOnAction(new DeleteAction());
		menu.getItems().add(deleteItem);

		shareItem = new CustomMenuItem(new Label("Share"));
		shareItem.setOnAction(new ShareFolderAction());
		menu.getItems().add(shareItem);

		propertiesItem = new MenuItem("Properties");
		propertiesItem.setOnAction(new ShowPropertiesAction());
		menu.getItems().add(propertiesItem);

		forceSyncItem = new MenuItem("Force synchronization");
		forceSyncItem.setOnAction(new ForceSyncAction());
		menu.getItems().add(forceSyncItem);

		recoverMenuItem = createRecoveMenuItem();
		menu.getItems().add(recoverMenuItem);

		menu.setOnShowing(new ShowMenuHandler());
        setContextMenu(menu);
	}

	private MenuItem createRecoveMenuItem() {
		Label label = new Label("Recover File");
		MenuItem menuItem = new CustomMenuItem(label);
		menuItem.setOnAction(new RecoverFileAction());
		return menuItem;
	}

	private class ShowMenuHandler implements EventHandler<WindowEvent> {
		@Override
		public void handle(WindowEvent arg0) {

			if(getItem() != null) {
				// first enable all and then decide depending on file/folder
				enableAllMenuItems();

				if (getItem().isFile()) {
					shareItem.setVisible(false);
					propertiesItem.setVisible(true);
				} else if (!getItem().getPath().toFile().exists()) {
					shareItem.setDisable(true);
					Label label = (Label) shareItem.getContent();
					label.setTooltip(new Tooltip("You cannot share this folder as it is not synchronized."));
				} else {
					shareItem.setDisable(false);
					shareItem.setVisible(true);

					Label label = (Label) shareItem.getContent();
					label.setTooltip(new Tooltip("Right-click to share this folder with a friend."));
				}

				boolean disableRecoverMenu = getItem().isFolder();
				recoverMenuItem.setDisable(disableRecoverMenu);
			} else {
				// disable menu
				disableAllMenuItems();
			}
		}

		private void disableAllMenuItems() {
			menu.getItems().forEach(item -> item.setDisable(true));
		}

		private void enableAllMenuItems() {
			menu.getItems().forEach(item -> item.setDisable(false));
		}
	}

	private class ShowPropertiesAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent arg0) {
			if (getItem() != null) {
				showProperties(getItem());
			}
		}

		private void showProperties(PathItem item) {
			try {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource(ViewNames.PROPERTIES_VIEW));
				loader.setController(new Properties(getItem()));

				Parent root = loader.load();

				// load UI on Application thread and show window
				Runnable showStage = new Runnable() {
					@Override
					public void run() {
						Scene scene = new Scene(root);
						stage = new Stage();
						stage.setTitle("Properties of "
								+ getItem().getPath().getFileName());
						stage.setScene(scene);
						stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
							@Override
							public void handle(WindowEvent event) {
								stage = null;
							}
						});

						stage.show();
					}
				};

				if (Platform.isFxApplicationThread()) {
					showStage.run();
				} else {
					Platform.runLater(showStage);
				}
			} catch (IOException e) {
				logger.warn("Exception while showing properties.", e);
			}
		}
	}

	private class DeleteAction implements EventHandler<ActionEvent> {
		public void handle(ActionEvent t) {
			if (getItem() != null) {
				Alert hardDelete = DialogUtils.createAlert(AlertType.WARNING);
				hardDelete.setTitle("Irreversibly delete file?");
				hardDelete.setHeaderText("You're about to hard-delete " + getItem().getPath().getFileName());
				hardDelete.setContentText("The file will be removed completely from the network and cannot be recovered."
						+ " Proceed?");
				hardDelete.getButtonTypes().clear();
				hardDelete.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

				hardDelete.showAndWait();

				if(hardDelete.getResult() == ButtonType.YES){
					fileEventManager.onLocalFileHardDelete(getItem().getPath());
					Alert confirm = DialogUtils.createAlert(AlertType.INFORMATION);
					confirm.setTitle("Hard-delete confirmation");
					confirm.setContentText(getItem().getPath() + " has been hard-deleted.");
					confirm.showAndWait();
				}
			}
		}
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

	private class ForceSyncAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent arg0) {
			IForceSyncHandler handler = forceSyncHandlerProvider.get();
			handler.forceSync();
		}
	}

}

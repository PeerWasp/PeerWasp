package org.peerbox.presenter.settings.synchronization;




import java.io.IOException;
import java.nio.file.Path;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.presenter.settings.Properties;
import org.peerbox.share.IShareFolderHandler;
import org.peerbox.share.ShareFolderController;
import org.peerbox.share.ShareFolderHandler;
import org.peerbox.view.ViewNames;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.stage.Stage;
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


	private Stage stage;
	

	private MenuItem recoverMenuItem;

	public CustomizedTreeCell(IFileEventManager fileEventManager,
			Provider<IFileRecoveryHandler> recoverFileHandlerProvider,
			Provider<IShareFolderHandler> shareFolderHandlerProvider){

		this.shareFolderHandlerProvider = shareFolderHandlerProvider;

		this.recoverFileHandlerProvider = recoverFileHandlerProvider;

		CustomMenuItem deleteItem = new CustomMenuItem(new Label("Delete from network"));
		Label shareLabel = new Label("Share");
		CustomMenuItem shareItem = new CustomMenuItem(shareLabel);
		MenuItem propertiesItem = new MenuItem("Properties");

		shareItem.setOnAction(new ShareFolderAction());

		recoverMenuItem = createRecoveMenuItem();

		menu.getItems().add(deleteItem);
		menu.getItems().add(recoverMenuItem);
		menu.getItems().add(shareItem);
		menu.getItems().add(propertiesItem);

		menu.setOnShowing(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				if(getItem().isFile()){
					shareItem.setVisible(false);
					propertiesItem.setVisible(true);
				} else if(!getItem().getPath().toFile().exists()){
					shareItem.setDisable(true);
					Label label = (Label)shareItem.getContent();
					label.setTooltip(new Tooltip("You cannot share this folder as it is not synchronized."));
				} else {
					shareItem.setDisable(false);
					shareItem.setVisible(true);
//					Rectangle rect = new Rectangle();
//					rect.setWidth(200);
//					rect.setHeight(100);

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
		
		propertiesItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				showProperties(getItem());
			}

			private void showProperties(PathItem item) {
				try {
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(getClass().getResource(
							ViewNames.PROPERTIES_VIEW));

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
					e.printStackTrace();
				}
			}
		});

		deleteItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				Action response = Dialogs.create()
		        .title("You're about to irreversibly hard-delete this file from the network!")
		        .masthead(null)
		        .message("Do you really want to hard-delete" + getItem().getPath() + "?" +
		        		" The file is automatically deleted from all devices and cannot be "
		        		+ "reconstructed.")
		        .actions(Dialog.ACTION_OK, Dialog.ACTION_CANCEL)
		        .showConfirm();
				if(response == Dialog.ACTION_OK){
					fileEventManager.onLocalFileHardDelete(getItem().getPath());
					Dialogs.create()
					        .title("PeerWasp Information")
					        .masthead(null)
					        .message(getItem().getPath() + " has been hard-deleted.")
					        .showInformation();
				}
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

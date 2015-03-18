package org.peerbox.share;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.ResultStatus;
import org.peerbox.utils.AlertUtils;
import org.peerbox.utils.IconUtils;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * This class is responsible for initializing and loading the GUI where the user can specify details
 * regarding sharing a folder (e.g. permissions, usernam, ...).
 *
 * @author albrecht
 *
 */
public class ShareFolderUILoader {

	private static final Logger logger = LoggerFactory.getLogger(ShareFolderUILoader.class);

	private Path folderToShare;

	private Stage stage;
	private ShareFolderController controller;

	@Inject
	public ShareFolderUILoader(ShareFolderController controller) {
		this.controller = controller;
	}

	/**
	 * Loads and shows the GUI.
	 *
	 * Precondition: folderToShare must be set.
	 */
	public void loadUi() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(ViewNames.SHARE_FOLDER_VIEW));
			// we set the controller manually because GuiceFxmlLoader would use parent injector
			loader.setController(controller);
			controller.setFolderToShare(folderToShare);
			Parent root = loader.load();

			// load UI on Application thread and show window
			Runnable showStage = new Runnable() {
				@Override
				public void run() {
					Scene scene = new Scene(root);
					stage = new Stage();
					stage.setTitle("Share Folder");
					stage.setScene(scene);
					Collection<Image> icons = IconUtils.createWindowIcons();
					stage.getIcons().addAll(icons);

					stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
						@Override
						public void handle(WindowEvent event) {
							stage = null;
							controller = null;
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
			logger.error("Could not load share folder stage: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Shows an error dialog to the user informing that sharing the folder failed.
	 *
	 * @param res the error result status
	 */
	public static void showError(ResultStatus res) {
		showError(res.getErrorMessage());
	}

	private static void showError(final String message) {
		Runnable dialog = new Runnable() {
			@Override
			public void run() {
				Alert dlg = AlertUtils.create(AlertType.ERROR);
				dlg.setTitle("Error Share Folder.");
				dlg.setHeaderText("Could not share folder.");
				dlg.setContentText(message);
				dlg.showAndWait();
			}
		};

		if (Platform.isFxApplicationThread()) {
			dialog.run();
		} else {
			Platform.runLater(dialog);
		}
	}

	/**
	 * Set the folder to share. Must be set before loading the GUI.
	 *
	 * @param folderToShare path to the folder
	 */
	public void setFolderToShare(Path folderToShare) {
		this.folderToShare = folderToShare;
	}

}

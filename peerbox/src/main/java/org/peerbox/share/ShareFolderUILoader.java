package org.peerbox.share;

import java.io.IOException;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.ResultStatus;
import org.peerbox.utils.IconUtils;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ShareFolderUILoader {

	private static final Logger logger = LoggerFactory.getLogger(ShareFolderUILoader.class);

	private Path folderToShare;

	private Stage stage;
	private ShareFolderController controller;

	@Inject
	public ShareFolderUILoader(ShareFolderController controller) {
		this.controller = controller;
	}

	public void loadUi() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(ViewNames.SHARE_FOLDER_VIEW));
			// we set the controller manually because GuiceFxmlLoader would use parent injector
			loader.setController(controller);
			Parent root = loader.load();
			controller.setFolderToShare(folderToShare);

			// load UI on Application thread and show
			Platform.runLater(() -> {

				Scene scene = new Scene(root);
				stage = new Stage();
				stage.setTitle("Share Folder");
				stage.setScene(scene);

				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						controller.cancel();
						stage = null;
						controller = null;
					}
				});

				stage.show();

			});

		} catch (IOException e) {
			logger.error("Could not load share folder stage: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	public static void showError(ResultStatus res) {
		showError(res.getErrorMessage());
	}

	private static void showError(final String message) {
		Runnable dialog = new Runnable() {
			@Override
			public void run() {
				Alert dlg = new Alert(AlertType.ERROR);
				IconUtils.decorateDialogWithIcon(dlg);
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

	public void setFolderToShare(Path folderToShare) {
		this.folderToShare = folderToShare;
	}

}

package org.peerbox.filerecovery;

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
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class FileRecoveryUILoader {
	
	private static final Logger logger = LoggerFactory.getLogger(FileRecoveryUILoader.class);

	private Path fileToRecover;

	private IFxmlLoaderProvider fxmlLoaderProvider;
	private Stage stage;
	private RecoverFileController controller;
	
	public void loadUi() {
		try {
			FXMLLoader loader = fxmlLoaderProvider.create("/view/RecoverFileView.fxml");
			Parent root = loader.load();
			controller = loader.getController();
			controller.setFileToRecover(fileToRecover);
			controller.loadVersions();
			
			// load UI on Application thread and show
			Platform.runLater(() -> {
				
				Scene scene = new Scene(root);
				stage = new Stage();
				stage.setTitle("File Recovery");
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
			logger.error("Could not load settings stage: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	public void showError(ResultStatus res) {
		showError(res.getErrorMessage());
	}
	
	private void showError(final String message) {
		Runnable dialog = new Runnable() {
			@Override
			public void run() {
				Alert dlg = new Alert(AlertType.ERROR);
				dlg.setTitle("Error Recovering File.");
				dlg.setHeaderText("Could not recover file.");
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
	
	public void setFileToRecover(Path fileToRecover) {
		this.fileToRecover = fileToRecover;
	}

	@Inject
	public void setFxmlLoaderProvider(IFxmlLoaderProvider fxmlLoaderProvider) {
		this.fxmlLoaderProvider = fxmlLoaderProvider;
	}

}

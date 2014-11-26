package org.peerbox.view;

import java.io.IOException;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.guice.GuiceFxmlLoader;
import org.peerbox.interfaces.IFileVersionSelectionUI;
import org.peerbox.presenter.RecoverFileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class RecoverFileStage implements IFileVersionSelectionUI {
	
	private static final Logger logger = LoggerFactory.getLogger(RecoverFileStage.class);
	private Stage stage;
	private GuiceFxmlLoader guiceFxmlLoader;
	private Path fileToRecover;
	private RecoverFileController controller;
	
	@Inject
	public void setGuiceFxmlLoader(GuiceFxmlLoader guiceFxmlLoader) {
		this.guiceFxmlLoader = guiceFxmlLoader;
	}
	
	private void load() {
		try {
			FXMLLoader loader = guiceFxmlLoader.create("/view/RecoverFileView.fxml");
			Parent root = loader.load();
			controller = loader.getController();
			controller.setFileToRecover(fileToRecover);
			Scene scene = new Scene(root);
			stage = new Stage();
			stage.setTitle("File Recovery");
			stage.setScene(scene);
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					stage = null;
				}
			});

		} catch (IOException e) {
			logger.error("Could not load settings stage: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void show() {
		Platform.runLater(() -> { 
			load();
			stage.show();
		});
	}

	@Override
	public void setFileToRecover(Path fileToRecover) {
		this.fileToRecover = fileToRecover;
	}
	
	
}

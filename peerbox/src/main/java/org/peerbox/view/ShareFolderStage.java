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
import org.peerbox.presenter.ShareFolderController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ShareFolderStage {
	
	
	private static final Logger logger = LoggerFactory.getLogger(ShareFolderStage.class);
	private Stage stage;
	private GuiceFxmlLoader guiceFxmlLoader;
	private Path folderToShare;
	private ShareFolderController controller;
	
	@Inject
	public void setGuiceFxmlLoader(GuiceFxmlLoader guiceFxmlLoader) {
		this.guiceFxmlLoader = guiceFxmlLoader;
	}
	
	private void load() {
		try {
			FXMLLoader loader = guiceFxmlLoader.create("/view/ShareFolderView.fxml");
			Parent root = loader.load();
			controller = loader.getController();
			controller.setFolderToShare(folderToShare);
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
	
	public void show() {
		Platform.runLater(() -> { 
			load();
			stage.show();
		});
	}

	public void setFolderToShare(Path folderToShare) {
		this.folderToShare = folderToShare;
	}
		
}

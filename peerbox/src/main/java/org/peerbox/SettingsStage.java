package org.peerbox;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SettingsStage {
	private static final Logger logger = LoggerFactory.getLogger(SettingsStage.class);

	private Stage stage;
	private IFxmlLoaderProvider fxmlLoaderProvider;
	
	@Inject
	public void setFxmlLoaderProvider(IFxmlLoaderProvider fxmlLoaderProvider) {
		this.fxmlLoaderProvider = fxmlLoaderProvider;
	}
	
	private void load() {
		try {
			FXMLLoader loader = fxmlLoaderProvider.create(ViewNames.SETTINGS_MAIN);
			Parent root = loader.load();
			Scene scene = new Scene(root, 600, 450);
			stage = new Stage();
			stage.setTitle("Settings");
			stage.setScene(scene);
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					logger.debug("SettingsStage closed.");
					stage = null; 
				}
			});

		} catch (IOException e) {
			logger.error("Could not load settings stage: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void show() {
		// load and if already loaded just show again and bring window to front.
		if(!isLoaded()) {
			load();
		}
		stage.show();
		stage.setIconified(false);
		stage.toFront();
	}

	private boolean isLoaded() {
		return stage != null;
	}
}

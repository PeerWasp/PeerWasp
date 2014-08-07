package org.peerbox;

import java.io.IOException;

import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SettingsStage {
	private static final Logger logger = LoggerFactory.getLogger(SettingsStage.class);
	
	private static SettingsStage instance = null;
	private Stage stage;
	
	public SettingsStage() {
		if(instance == null) {
			instance = this;
			
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(SettingsStage.class.getResource(ViewNames.SETTINGS_MAIN));
			try {
				Parent root = loader.load();
				Scene scene = new Scene(root, 600, 450);
				stage = new Stage();
			    stage.setTitle("Settings");
			    stage.setScene(scene);
			    
			    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						instance.stage = null;
						instance = null;
					}
				});

			} catch (IOException e) {
				logger.error("Could not load settings stage: {}", e.getMessage());
				e.printStackTrace();
			}
		}
		

	}
	
	public void show() {
		instance.stage.show();
		instance.stage.setIconified(false);
		instance.stage.toFront();
	}
}

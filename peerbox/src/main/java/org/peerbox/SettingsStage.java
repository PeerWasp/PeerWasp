package org.peerbox;

import java.io.IOException;

import org.peerbox.presenter.MainController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SettingsStage {
	public SettingsStage() {
		
	
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainController.class.getResource("/org/peerbox/view/settings/Main.fxml"));
		try {
			Parent root = loader.load();
			Scene scene = new Scene(root, 600, 450);
			Stage stage = new Stage();
		    stage.setTitle("Settings - PeerBox");
		    stage.setScene(scene);
		    stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

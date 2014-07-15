package org.peerbox.controller;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class MainNavigator {
	
	private static MainController mainController;
	
	public static void setMainController(MainController controller) {
		mainController = controller;
	}
	
	public static void navigate(String fxmlFile) {
		Node content = null;
		try {
			content = FXMLLoader.load(MainController.class.getResource(fxmlFile));
			mainController.setContent(content);
		} catch(IOException e) {
			System.err.println("Could not load fxml file.");
		}
	}
}

package org.peerbox.view;

import java.io.IOException;

import org.peerbox.presenter.MainController;
import org.peerbox.presenter.MainNavigator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginView  extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
    public void start(Stage primaryStage) {  	
		// TODO: we probably need a better way to deal with the dependnecies. 
		// the dependency on the H2HManager always means that a network is required for testing 
		
    	Pane root;
    	MainController mainController;
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/peerbox/view/MainView.fxml"));
			root = (Pane) fxmlLoader.load();
			mainController = (MainController)fxmlLoader.getController();
			
			MainNavigator.setMainController(mainController);
			MainNavigator.navigate("/org/peerbox/view/LoginView.fxml");
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
    }
}

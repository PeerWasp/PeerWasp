package org.peerbox;


import java.io.IOException;

import org.peerbox.presenter.MainController;
import org.peerbox.presenter.MainNavigator;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * This is the first prototype of graphical user interface.
 */
public class App extends Application
{
	public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    	
    	primaryStage.setTitle("PeerBox");
    	
    	Pane root;
    	MainController mainController;
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/peerbox/view/MainView.fxml"));
			root = (Pane) fxmlLoader.load();
			mainController = (MainController)fxmlLoader.getController();
			
			MainNavigator.setMainController(mainController);
			MainNavigator.navigate("/org/peerbox/view/NetworkSelectionWindow.fxml");
			
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


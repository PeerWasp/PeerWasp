package org.peerbox;


import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.peerbox.PropertyHandler;
import org.peerbox.model.H2HManager;
import org.peerbox.presenter.MainController;
import org.peerbox.presenter.MainNavigator;

/**
 * This is the first prototype of graphical user interface.
 */
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger("PeerBox");
	
	public static void main(String[] args) {
		
		logger.info("PeerBox started.");
		PropertyHandler.checkFileExists();
		PropertyHandler.loadPropertyFile();
		H2HManager.INSTANCE.setRootPath(PropertyHandler.getRootPath());
        
		launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    	
    	primaryStage.setTitle("PeerBox");
    	primaryStage.getIcons().add(new Image("/org/peerbox/view/icon.png"));
    	
    	installExitHandler(primaryStage);
    	  	
    	
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

	private void installExitHandler(Stage stage) {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                // TODO: close application properly or hide to tray.
            	Platform.exit();
                System.exit(0);
            }

        });
	}
}


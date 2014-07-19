package org.peerbox;


import java.io.IOException;
import java.net.URL;

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
import org.peerbox.view.ViewNames;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This is the first prototype of graphical user interface.
 */
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger("PeerBox");
	
	private Injector injector;

	private H2HManager h2hManager; 
	
	public static void main(String[] args) {
		logger.info("PeerBox started.");
		
		PropertyHandler.checkFileExists();
		PropertyHandler.loadPropertyFile();
        
		launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    	
    	initializeGuice();
    	
    	h2hManager = injector.getInstance(H2HManager.class);
    	h2hManager.setRootPath(PropertyHandler.getRootPath());
    	
    	installExitHandler(primaryStage);
    	
    	Pane root;
    	MainController mainController;
		try {
			FXMLLoader fxmlLoader = MainNavigator.createGuiceFxmlLoader(ViewNames.MAIN_VIEW);
			root = fxmlLoader.load();
			mainController = (MainController)fxmlLoader.getController();
			
			MainNavigator.setMainController(mainController);
			MainNavigator.navigate(ViewNames.NETWORK_SELECTION_VIEW);
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Scene scene = new Scene(root);
		primaryStage.setTitle("PeerBox");
    	primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
    }

	private void initializeGuice() {
		injector = Guice.createInjector(new PeerBoxModule());
		MainNavigator.setInjector(injector);
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


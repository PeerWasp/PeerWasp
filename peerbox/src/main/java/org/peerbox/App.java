package org.peerbox;


import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.peerbox.PropertyHandler;
import org.peerbox.interfaces.INavigatable;
import org.peerbox.model.H2HManager;
import org.peerbox.presenter.NavigationService;
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
	private NavigationService navigationService;
	private H2HManager h2hManager; 
	
	//offset values are needed for calculating position of application
	private double xOffset = 0;
	private double yOffset = 0;
	
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
    	navigationService = injector.getInstance(NavigationService.class);
    	navigationService.setInjector(injector);
    	
    	Parent root;
    	INavigatable mainController;
		try {
			FXMLLoader fxmlLoader = navigationService.createGuiceFxmlLoader(ViewNames.MAIN_VIEW);
			root = fxmlLoader.load();
			mainController = (INavigatable)fxmlLoader.getController();
			
			navigationService.setNavigationController(mainController);
			navigationService.navigate(ViewNames.NETWORK_SELECTION_VIEW);
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Scene scene = new Scene(root, 275, 500);
		primaryStage.setTitle("PeerBox");
    	primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
    	primaryStage.initStyle(StageStyle.TRANSPARENT);
    	installExitHandler(primaryStage);
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
		
        //provides ability to move application window wherever the user clicks & drags with the mouse
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
		
		//releases window at position when mouse click is released
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });
    }
    
    

	private void initializeGuice() {
		injector = Guice.createInjector(new PeerBoxModule());
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


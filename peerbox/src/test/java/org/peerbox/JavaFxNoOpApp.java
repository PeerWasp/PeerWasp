package org.peerbox;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is required to initialize the JavaFX toolkit (start the JavaFX thread etc.).
 * It dies nothing in particular, but gets called by the toolkit after launch().
 * @author albrecht
 *
 */
public class JavaFxNoOpApp extends Application {
	
	private static boolean initialized = false;
	
	public JavaFxNoOpApp() {
		initialized = true;
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// NO OP - just here to initialize toolkit
	}
	
	public static boolean isInitialized() {
		return initialized;
	}
}
package org.peerbox.helper;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is required to initialize the JavaFX toolkit (start the JavaFX thread etc.).
 * It dies nothing in particular, but gets called by the toolkit after launch().
 * @author albrecht
 *
 */
public class JavaFxNoOpApp extends Application {

	private final static AtomicBoolean initialized = new AtomicBoolean(false);

	public JavaFxNoOpApp() {
		initialized.set(true);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// NO OP - just here to initialize toolkit
	}

	public static boolean isInitialized() {
		return initialized.get();
	}
}
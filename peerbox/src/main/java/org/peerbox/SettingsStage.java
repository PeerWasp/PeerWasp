package org.peerbox;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.app.AppContext;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class SettingsStage {

	private static final Logger logger = LoggerFactory.getLogger(SettingsStage.class);

	/* window properties */
	private static final String WINDOW_TITLE = "Settings";
	private static final double WINDOW_WIDTH = 600.0;
	private static final double WINDOW_HEIGHT = 450.0;

	private IFxmlLoaderProvider fxmlLoaderProvider;
	private AppContext appContext;
	private Stage stage;

	@Inject
	public SettingsStage(AppContext appContext) {
		this.appContext = appContext;
	}

	@Inject
	public void setFxmlLoaderProvider(IFxmlLoaderProvider fxmlLoaderProvider) {
		this.fxmlLoaderProvider = fxmlLoaderProvider;
	}

	private void load() {
		try {
			Preconditions.checkNotNull(appContext.getCurrentClientContext(), "ClientContext must not be null.");

			// important: use injector for client here because of client specific instances.
			FXMLLoader loader = fxmlLoaderProvider.create(ViewNames.SETTINGS_MAIN, appContext.getCurrentClientContext().getInjector());
			Parent root = loader.load();
			Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
			stage = new Stage();
			stage.setTitle(WINDOW_TITLE);
			stage.setScene(scene);
			stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new WindowCloseRequestEventHandler());

		} catch (IOException e) {
			logger.error("Could not load settings stage: {}", e.getMessage(), e);
		}
	}

	public void show() {
		final Runnable show = new Runnable() {
			@Override
			public void run() {
				// load and if already loaded just show again and bring window to front.
				if (!isLoaded()) {
					load();
				}
				stage.show();
				stage.setIconified(false);
				stage.toFront();
				stage.requestFocus();
			}
		};

		if (Platform.isFxApplicationThread()) {
			show.run();
		} else {
			Platform.runLater(show);
		}
	}

	private boolean isLoaded() {
		return stage != null;
	}

	private class WindowCloseRequestEventHandler implements EventHandler<WindowEvent> {
		@Override
		public void handle(WindowEvent event) {
			logger.debug("SettingsStage closed.");
			stage = null;
		}
	}
}

package org.peerbox.app.activity;

import java.io.IOException;
import java.util.Collection;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.guice.IFxmlLoaderProvider;
import org.peerbox.utils.IconUtils;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Responsible for loading and configuring the activity window (GUI part).
 *
 * @author albrecht
 *
 */
public class ActivityStage {

	private static final Logger logger = LoggerFactory.getLogger(ActivityStage.class);

	/* window properties */
	private static final String WINDOW_TITLE = "Activity";
	private static final double WINDOW_WIDTH = 600.0;
	private static final double WINDOW_HEIGHT = 450.0;

	private IFxmlLoaderProvider fxmlLoaderProvider;
	private Stage stage;

	@Inject
	public void setFxmlLoaderProvider(IFxmlLoaderProvider fxmlLoaderProvider) {
		this.fxmlLoaderProvider = fxmlLoaderProvider;
	}

	private void load() {
		try {

			FXMLLoader loader = fxmlLoaderProvider.create(ViewNames.ACTIVITY_VIEW);
			Parent root = loader.load();
			Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
			stage = new Stage();
			stage.setTitle(WINDOW_TITLE);

			Collection<Image> icons = IconUtils.createWindowIcons();
			stage.getIcons().addAll(icons);

			stage.setScene(scene);
			stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new WindowCloseRequestEventHandler());

		} catch (IOException e) {
			logger.error("Could not load activity stage: {}", e.getMessage(), e);
		}
	}

	/**
	 * Create and show Activity Window. If there is already an instance showing, this will not
	 * create an additional instance but rather show the existing instance.
	 */
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
			logger.debug("ActivityStage closed.");
			stage = null;
		}
	}
}

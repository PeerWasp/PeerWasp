package org.peerbox;

import java.io.IOException;
import java.util.Collection;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.peerbox.app.Constants;
import org.peerbox.app.IExitHandler;
import org.peerbox.presenter.INavigatable;
import org.peerbox.presenter.NavigationService;
import org.peerbox.utils.DialogUtils;
import org.peerbox.utils.IconUtils;
import org.peerbox.view.ViewNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class StartupStage {
	private static final Logger logger = LoggerFactory.getLogger(StartupStage.class);
	private Stage stage;
	private Parent mainView;
	private NavigationService navigationService;
	private IExitHandler exitHandler;


	public StartupStage() {
	}

	public NavigationService getNavigationService() {
		return navigationService;
	}

	@Inject
	public void setNavigationService(NavigationService service) {
		this.navigationService = service;
	}

	@Inject
	public void setStage(@Named("PrimaryStage") Stage stage) {
		this.stage = stage;
	}

	@Inject
	public void setExitHandler(IExitHandler exitHandler) {
		this.exitHandler = exitHandler;
	}

	private void initializeStage() {
		stage.setTitle(Constants.APP_NAME);
		Collection<Image> icons = IconUtils.createWindowIcons();
		stage.getIcons().addAll(icons);
		stage.initStyle(StageStyle.DECORATED);
		installExitHandler();

		Scene scene = new Scene(mainView);
    	stage.setScene(scene);
    	stage.sizeToScene();

		// Add configuration of stage here
	}

	private void installExitHandler() {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	        @Override
	        public void handle(WindowEvent t) {
	            exitHandler.exit();
	        }
	    });
	}

	private void initializeMainView() {
		INavigatable mainController;
		try {
			FXMLLoader fxmlLoader = navigationService.createLoader(ViewNames.MAIN_VIEW);
			mainView = fxmlLoader.load();
			mainController = (INavigatable)fxmlLoader.getController();

			navigationService.setNavigationController(mainController);

		} catch (IOException e) {
			logger.error("Could not load initial view (main view and controller): {}", e.getMessage());
			showErrorMsg(e.getMessage());
			return;
		}
	}

	private void showErrorMsg(String msg) {
		final Runnable msgRunner = new Runnable() {
			@Override
			public void run() {
				Alert dlg = DialogUtils.createAlert(AlertType.ERROR);
				dlg.setTitle("Opening Window Failed");
				dlg.setHeaderText("Could not open window");
				dlg.setContentText(String.format("Message: %s", msg));
				dlg.showAndWait();
			}
		};
		if (Platform.isFxApplicationThread()) {
			msgRunner.run();
		} else {
			Platform.runLater(msgRunner);
		}
	}

	public void show() {
		initializeMainView();
    	initializeStage();
    	navigationService.navigate(ViewNames.NETWORK_SELECTION_VIEW);
    	stage.show();
	}
}

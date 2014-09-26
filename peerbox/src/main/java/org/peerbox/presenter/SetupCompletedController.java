package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.ResultStatus;
import org.peerbox.model.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SetupCompletedController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(SetupCompletedController.class);
	
	@FXML 
	private Pane pane;
	
	private NavigationService fNavigationService;
	private UserManager fUserManager;
	
	@Inject
	public SetupCompletedController(NavigationService navigationService, UserManager userManager) {
		this.fNavigationService = navigationService;
		this.fUserManager = userManager;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	public void closeWindowAction(ActionEvent event) {
		// Sample event for closing window -> application should still be running in the tray
		// TODO: can only do this if tray is supported
		Stage stage = (Stage) pane.getScene().getWindow();
		stage.close();
	}
	
	public void logoutAction(ActionEvent event) {
		Task<ResultStatus> task = createLogoutTask();
		new Thread(task).start();
	}

	private ResultStatus logoutUser() {
		try {
			return fUserManager.logoutUser();
		} catch (InvalidProcessStateException | NoPeerConnectionException | NoSessionException | InterruptedException e) {
			e.printStackTrace();
			return ResultStatus.error(e.getMessage());
		}
	}
	
	private void onLogoutFailed() {
		logger.warn("Logout failed.");
	}
	
	private void onLogoutSucceeded() {
		logger.info("Logout succeeded.");
	}

	private Task<ResultStatus> createLogoutTask() {
		Task<ResultStatus> task = new Task<ResultStatus>() {
			@Override
			public ResultStatus call() {
				return logoutUser();
			}
		};
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				onLogoutFailed();
			}
		});
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				ResultStatus result = task.getValue();
				if(result.isOk()) {
					onLogoutSucceeded();
				} else {
					onLogoutFailed();
				}
			}
		});
		return task;
	}
}

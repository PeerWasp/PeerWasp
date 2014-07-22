package org.peerbox.presenter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.peerbox.model.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;

public class SetupCompletedController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(SetupCompletedController.class);
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
	
	
	public void logoutAction(ActionEvent event) {
		Task<Boolean> task = createLogoutTask();
		new Thread(task).start();
	}

	private Boolean logoutUser() {
		boolean success = false;
		try {
			success = fUserManager.logoutUser();
		} catch (InvalidProcessStateException | NoPeerConnectionException | NoSessionException | InterruptedException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
		
	}
	
	private void onLogoutFailed() {
		logger.warn("Logout failed.");
	}
	
	private void onLogoutSucceeded() {
		logger.info("Logout succeeded.");
	}

	private Task<Boolean> createLogoutTask() {
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			public Boolean call() {
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
				try {
					if(task.get()) {
						onLogoutSucceeded();
					} else {
						onLogoutFailed();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					onLogoutFailed();
				}
			}
		});
		return task;
	}
}

package org.peerbox.view;

import javafx.stage.Stage;

import org.peerbox.view.tray.AbstractSystemTray;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class UiContext {

	private Stage primaryStage;

	private AbstractSystemTray systemTray;

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	@Inject
	protected void setPrimaryStage(@Named("PrimaryStage") Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public AbstractSystemTray getSystemTray() {
		return systemTray;
	}

	@Inject
	protected void setSystemTray(AbstractSystemTray systemTray) {
		this.systemTray = systemTray;
	}

}

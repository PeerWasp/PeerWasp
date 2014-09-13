package org.peerbox.presenter.tray;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;

import org.apache.commons.io.FileUtils;
import org.peerbox.SettingsStage;

import com.google.inject.Inject;

public class TrayActionHandler {

	@Inject 
	private SettingsStage settingsStage;
	
	public void openRootFolder() throws IOException {
		// TODO: this needs to be the root folder, not the user home folder...
		java.awt.Desktop.getDesktop().open(new File(FileUtils.getUserDirectoryPath()));
	}

	public void quit() {
		// TODO: proper exit handling. should not be done here,
		// but rather within the application somewhere (graceful disconnect etc)
		System.exit(0);
	}

	public void showSettings() {
		Platform.runLater(() -> {
			settingsStage.show();
		});
	}

}

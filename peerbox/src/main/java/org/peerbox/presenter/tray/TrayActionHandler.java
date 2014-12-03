package org.peerbox.presenter.tray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;

import org.apache.commons.io.FileUtils;
import org.peerbox.SettingsStage;
import org.peerbox.UserConfig;
import org.peerbox.app.IExitHandler;

import com.google.inject.Inject;

public class TrayActionHandler {

	private SettingsStage settingsStage;
	private IExitHandler exitHandler;
	private UserConfig userConfig;

	public void openRootFolder() throws IOException {
		Path toOpen = userConfig.getRootPath();
		if(toOpen == null || !Files.exists(toOpen)) {
			toOpen = Paths.get(FileUtils.getUserDirectoryPath());
		}
		java.awt.Desktop.getDesktop().open(toOpen.toFile());
	}

	public void quit() {
		exitHandler.exit();
	}

	public void showSettings() {
		Platform.runLater(() -> {
			settingsStage.show();
		});
	}
	
	@Inject
	public void setSettingsStage(SettingsStage settingsStage) {
		this.settingsStage = settingsStage;
	}
	
	@Inject
	public void setExitHandler(IExitHandler exitHandler) {
		this.exitHandler = exitHandler;
	}
	
	@Inject
	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}

}

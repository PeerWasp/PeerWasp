package org.peerbox.presenter.tray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.peerbox.SettingsStage;
import org.peerbox.app.IExitHandler;
import org.peerbox.app.activity.ActivityStage;
import org.peerbox.app.config.UserConfig;

import com.google.inject.Inject;

public class TrayActionHandler {

	private SettingsStage settingsStage;
	private ActivityStage activityStage;
	private IExitHandler exitHandler;
	private UserConfig userConfig;

	public void openRootFolder() throws IOException {
		// TODO: SwingUtilities.invokeLater!
		// TODO: only possible if user is logged in
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
		// TODO: only possible if user is logged in
		settingsStage.show();
	}

	public void showActivity() {
		activityStage.show();
	}

	@Inject
	public void setSettingsStage(SettingsStage settingsStage) {
		this.settingsStage = settingsStage;
	}

	@Inject
	public void setActivityStage(ActivityStage activityStage) {
		this.activityStage = activityStage;
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

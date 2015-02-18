package org.peerbox.presenter.tray;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.peerbox.SettingsStage;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.peerbox.app.IExitHandler;
import org.peerbox.app.activity.ActivityStage;
import org.peerbox.app.config.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TrayActionHandler {

	private static final Logger logger = LoggerFactory.getLogger(TrayActionHandler.class);

	private SettingsStage settingsStage;
	@Inject
	private AppContext appContext;
	@Inject
	private Provider<ActivityStage> activityStage;
	@Inject
	private Provider<IExitHandler> exitHandler;

	// TODO(AA) actions and menu should depende on logged in status etc.
	// dependency managemen

	public void openRootFolder() {
		ClientContext clientContext = appContext.getCurrentClientContext();
		if (clientContext != null) {
			UserConfig userConfig = clientContext.getUserConfig();
			Path toOpen = userConfig.getRootPath();
			if (toOpen == null || !Files.exists(toOpen)) {
				toOpen = FileUtils.getUserDirectory().toPath();
			}
			final Path toOpenF = toOpen; // invokeLater requires final path.
			SwingUtilities.invokeLater(() -> {
				try {
					java.awt.Desktop.getDesktop().open(toOpenF.toFile());
				} catch (Exception e) {
					logger.warn("Could not open folder.", e);
				}
			});
		}
	}

	public void quit() {
		exitHandler.get().exit();
	}

	public void showSettings() {
		// TODO: only possible if user is logged in
		settingsStage.show();
	}

	public void showActivity() {
		activityStage.get().show();
	}

	@Inject
	public void setSettingsStage(SettingsStage settingsStage) {
		this.settingsStage = settingsStage;
	}

}

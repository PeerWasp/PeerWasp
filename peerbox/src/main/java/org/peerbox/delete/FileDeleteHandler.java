package org.peerbox.delete;

import java.nio.file.Path;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileUtil;
import org.peerbox.ResultStatus;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.watchservice.IFileEventManager;

import com.google.inject.Inject;

public class FileDeleteHandler implements IFileDeleteHandler {

	private Path fileToDelete;

	private AppContext appContext;

	@Override
	public void deleteFile(final Path fileToDelete) {
		this.fileToDelete = fileToDelete;

		ResultStatus res = checkPreconditions();
		if (res.isOk()) {
			ClientContext clientContext = appContext.getCurrentClientContext();
			IFileEventManager fileEventManager = clientContext.getFileEventManager();
			fileEventManager.onLocalFileHardDelete(fileToDelete);
		} else {
			showError(res);
		}
	}

	private void showError(ResultStatus res) {
		showError(res.getErrorMessage());
	}

	private void showError(final String message) {
		Runnable dialog = new Runnable() {
			@Override
			public void run() {
				Alert dlg = new Alert(AlertType.ERROR);
				dlg.setTitle("Error - Delete.");
				dlg.setHeaderText("Could not delete file(s).");
				dlg.setContentText(message);
				dlg.showAndWait();
			}
		};

		if (Platform.isFxApplicationThread()) {
			dialog.run();
		} else {
			Platform.runLater(dialog);
		}
	}

	private ResultStatus checkPreconditions() {

		ClientContext clientContext = appContext.getCurrentClientContext();
		INodeManager nodeManager = null;
		IUserManager userManager = null;
		UserConfig userConfig = null;

		if (clientContext == null) {
			// if there is no client context, the user did not connect / log in yet
			return ResultStatus.error("There is no client connected and logged in.");
		}

		nodeManager = clientContext.getNodeManager();
		if (!nodeManager.isConnected()) {
			return ResultStatus.error("There is no connection to the network.");
		}

		userManager = clientContext.getUserManager();
		try {
			if (!userManager.isLoggedIn()) {
				return ResultStatus.error("The user is not logged in. Please login.");
			}
		} catch (NoPeerConnectionException e) {
			return ResultStatus.error("There is no connection to the network.");
		}

		userConfig = clientContext.getUserConfig();
		if (!FileUtil.isInH2HDirectory(fileToDelete.toFile(), userConfig.getRootPath().toFile())) {
			return ResultStatus.error("The file is not in the root directory.");
		}

		return ResultStatus.ok();
	}

	@Inject
	public void setAppContext(AppContext appContext) {
		this.appContext = appContext;
	}

}

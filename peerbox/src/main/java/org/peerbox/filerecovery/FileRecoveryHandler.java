package org.peerbox.filerecovery;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileUtil;
import org.peerbox.ResultStatus;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;

import com.google.inject.Inject;

public class FileRecoveryHandler implements IFileRecoveryHandler {

	private Path fileToRecover;

	private AppContext appContext;

	private FileRecoveryUILoader uiLoader;


	@Override
	public void recoverFile(final Path fileToRecover) {
		this.fileToRecover = fileToRecover;
		uiLoader.setFileToRecover(fileToRecover);

		ResultStatus res = checkPreconditions();
		if (res.isOk()) {
			uiLoader.loadUi();
		} else {
			uiLoader.showError(res);
		}
	}

	private ResultStatus checkPreconditions() {

		ClientContext clientContext = appContext.getCurrentClientContext();
		INodeManager nodeManager = null;
		IUserManager userManager = null;
		IFileManager fileManager = null;
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
		if (!FileUtil.isInH2HDirectory(fileToRecover.toFile(), userConfig.getRootPath().toFile())) {
			return ResultStatus.error("The file is not in the root directory.");
		}

		if (Files.isDirectory(fileToRecover)) {
			return ResultStatus.error("Recovery works only for files and not for folders.");
		}

		fileManager = clientContext.getFileManager();
		if(!fileManager.existsRemote(fileToRecover)) {
			return ResultStatus.error("File does not exist in the network.");
		}

		if(fileManager.isLargeFile(fileToRecover)) {
			return ResultStatus.error("File is too large, multiple versions are not supported.");
		}

		return ResultStatus.ok();
	}

	@Inject
	public void setFileRecoveryUILoader(FileRecoveryUILoader uiLoader) {
		this.uiLoader = uiLoader;
	}

	@Inject
	public void setAppContext(AppContext appContext) {
		this.appContext = appContext;
	}

}

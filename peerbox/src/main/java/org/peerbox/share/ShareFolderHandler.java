package org.peerbox.share;

import java.nio.file.Files;
import java.nio.file.Path;

import javafx.application.Platform;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileUtil;
import org.peerbox.ResultStatus;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Handles a request for sharing a folder.
 * First, preconditions are checked (e.g. is it a folder, does it exist in network, etc.).
 * In a second step, the UI is loaded and shown to the user such that he can specify the
 * username and the permissions.
 * The actual sharing invocation in response to the user operation is in
 * the {@link ShareFolderController}.
 *
 * @author albrecht
 *
 */
public final class ShareFolderHandler implements IShareFolderHandler {

	private static final Logger logger = LoggerFactory.getLogger(ShareFolderHandler.class);

	private Path folderToShare;
	private AppContext appContext;

	@Inject
	public ShareFolderHandler(AppContext appContext) {
		this.appContext = appContext;
	}

	private ShareFolderUILoader createUiLoader() {
		// we have to use the child injector of the current client because of the FileManager instance
		// which is specific to the current user.
		// the server, however, runs in the global context (AppContext) and was created by the
		// parent / main injector
		ClientContext clientContext = appContext.getCurrentClientContext();
		return clientContext.getInjector().getInstance(ShareFolderUILoader.class);
	}

	@Override
	public void shareFolder(Path folderToShare) {
		this.folderToShare = folderToShare;

		ResultStatus res = checkPreconditions();
		if (res.isOk()) {
			logger.info("Preconditions for sharing folder satisfied, loading UI. (Folder: {})", folderToShare);
			Platform.runLater(() -> {
				ShareFolderUILoader uiLoader = createUiLoader();
				uiLoader.setFolderToShare(this.folderToShare);
				uiLoader.loadUi();
			});
		} else {
			logger.warn("Preconditions for sharing folder NOT satisfied: {} (Folder: {})",
					res.getErrorMessage(), folderToShare);
			ShareFolderUILoader.showError(res);
		}
	}

	/**
	 * Checks preconditions for sharing folder:
	 * - connected
	 * - logged in
	 * - is a folder (not a file)
	 * - is in the root directory (not outside)
	 * - exists in network (user profile)
	 *
	 * @return ok status or error
	 */
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
		if (!FileUtil.isInH2HDirectory(folderToShare.toFile(), userConfig.getRootPath().toFile())) {
			return ResultStatus.error("The folder is not in the root directory.");
		}

		if (!Files.isDirectory(folderToShare)) {
			return ResultStatus.error("Sharing works only with folders and not files.");
		}

		fileManager = clientContext.getFileManager();
		if(!fileManager.existsRemote(folderToShare)) {
			return ResultStatus.error("Folder does not exist in the network.");
		}

		return ResultStatus.ok();
	}

}

package org.peerbox.share;

import java.nio.file.Path;

import javafx.application.Platform;

import org.peerbox.ResultStatus;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

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
			Platform.runLater(() -> {
				ShareFolderUILoader uiLoader = createUiLoader();
				uiLoader.setFolderToShare(this.folderToShare);
				uiLoader.loadUi();
			});
		} else {
			ShareFolderUILoader.showError(res);
		}
	}

	private ResultStatus checkPreconditions() {
		// TODO(AA)
		return ResultStatus.ok();
	}

}

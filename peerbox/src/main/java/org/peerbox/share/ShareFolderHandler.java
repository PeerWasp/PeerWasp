package org.peerbox.share;

import java.io.IOException;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.peerbox.ResultStatus;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.peerbox.filerecovery.FileRecoveryUILoader;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public final class ShareFolderHandler implements IShareFolderHandler {


	private static final Logger logger = LoggerFactory.getLogger(ShareFolderHandler.class);
//	private Stage stage;
//	private IFxmlLoaderProvider fxmlLoaderProvider;

	private Path folderToShare;
	private AppContext appContext;

//	private ShareFolderController controller;

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

//	private void load() {
//		try {
//			FXMLLoader loader = fxmlLoaderProvider.create("/view/ShareFolderView.fxml");
//			Parent root = loader.load();
//			controller = loader.getController();
//			controller.setFolderToShare(folderToShare);
//			Scene scene = new Scene(root);
//			stage = new Stage();
//			stage.setTitle("File Recovery");
//			stage.setScene(scene);
//			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//				@Override
//				public void handle(WindowEvent event) {
//					stage = null;
//				}
//			});
//
//			stage.show();
//
//		} catch (IOException e) {
//			logger.error("Could not load settings stage: {}", e.getMessage());
//			e.printStackTrace();
//		}
//	}


	@Override
	public void shareFolder(Path folderToShare) {
		this.folderToShare = folderToShare;

		ResultStatus res = checkPreconditions();
		if(res.isOk()) {
			Platform.runLater(() -> {
				ShareFolderUILoader uiLoader = createUiLoader();
				uiLoader.setFolderToShare(this.folderToShare);
				uiLoader.loadUi();
			});
		} else {
			// TODO: show error message
		}
	}

	private ResultStatus checkPreconditions() {
		return ResultStatus.ok();
	}

//	public void setFolderToShare(Path folderToShare) {
//		this.folderToShare = folderToShare;
//	}

}

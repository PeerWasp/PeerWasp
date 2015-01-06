package org.peerbox.filerecovery;

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
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class FileRecoveryHandler implements IFileRecoveryHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(FileRecoveryHandler.class);
	private Stage stage;
	private IFxmlLoaderProvider fxmlLoaderProvider;
	private Path fileToRecover;
	private RecoverFileController controller;
	private INodeManager h2hManager; 
	private IUserManager userManager;
	
	@Inject
	public void setH2HManager(INodeManager h2hManager) {
		this.h2hManager = h2hManager;
	}
	
	@Inject
	public void setFxmlLoaderProvider(IFxmlLoaderProvider fxmlLoaderProvider) {
		this.fxmlLoaderProvider = fxmlLoaderProvider;
	}
	
	private void load() {
		try {
			FXMLLoader loader = fxmlLoaderProvider.create("/view/RecoverFileView.fxml");
			Parent root = loader.load();
			controller = loader.getController();
			controller.setFileToRecover(fileToRecover);
			Scene scene = new Scene(root);
			stage = new Stage();
			stage.setTitle("File Recovery");
			stage.setScene(scene);
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					controller.cancel();
					stage = null;
					controller = null;
				}
			});
			
			
			Platform.runLater(() -> { 
				stage.show();
			});

		} catch (IOException e) {
			logger.error("Could not load settings stage: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void recoverFile(Path fileToRecover) {
		this.fileToRecover = fileToRecover;
		
		ResultStatus res = checkPreconditions();
		if(res.isOk()) {
			load();
		} else {
			// TODO: show error message
		}
	}

	private ResultStatus checkPreconditions() {
		
//		
//		if(!h2hManager.isConnected()) {
//			return ResultStatus.error("There is no connection to the network");
//		}
		
		
		// TODO: we need to ensure:
		// - connected
		// - logged in
		// - file is in the root folder somewhere (not outside)
		// - versions only possible for files (not folders) -> need to check this in the profile (not on disk)
		// ?
		
		return ResultStatus.ok();
	}
}

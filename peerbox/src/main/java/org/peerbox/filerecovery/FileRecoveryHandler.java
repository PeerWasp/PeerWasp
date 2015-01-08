package org.peerbox.filerecovery;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileUtil;
import org.peerbox.IPeerboxFileManager;
import org.peerbox.IUserConfig;
import org.peerbox.ResultStatus;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;

import com.google.inject.Inject;

public class FileRecoveryHandler implements IFileRecoveryHandler {
	
	private Path fileToRecover;
	
	private IUserConfig userConfig;
	private INodeManager nodeManager; 
	private IPeerboxFileManager fileManager;
	private IUserManager userManager;
	
	private FileRecoveryUILoader uiLoader;
	
	public FileRecoveryHandler() {

	}
	
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

		if (!nodeManager.isConnected()) {
			return ResultStatus.error("There is no connection to the network.");
		}

		try {
			if (!userManager.isLoggedIn()) {
				return ResultStatus.error("The user is not logged in. Please login.");
			}
		} catch (NoPeerConnectionException e) {
			return ResultStatus.error("There is no connection to the network.");
		}

		if (!FileUtil.isInH2HDirectory(fileToRecover.toFile(), userConfig.getRootPath().toFile())) {
			return ResultStatus.error("The file is not in the root directory.");
		}

		if (Files.isDirectory(fileToRecover)) {
			return ResultStatus.error("Recovery works only for files and not for folders.");
		}
		
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
	public void setUserConfig(IUserConfig userConfig) {
		this.userConfig = userConfig;
	}
	
	@Inject
	public void setNodeManager(INodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}
	
	@Inject
	public void setUserManager(IUserManager userManager) {
		this.userManager = userManager;
	}
	
	@Inject
	public void setFileManager(IPeerboxFileManager fileManager) {
		this.fileManager = fileManager;
	}
	
}

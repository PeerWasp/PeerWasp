package org.peerbox.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStarter {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStarter.class);
	
	protected static final Path BASE_PATH = Paths.get(FileUtils.getUserDirectory().getAbsolutePath(), "PeerBox_Test");
	
	protected UserCredentials credentials;
	
	public AbstractStarter() throws IOException {
		createBasePath();
	}
	
	protected void createBasePath() throws IOException {
		if (!Files.exists(BASE_PATH)) {
			Files.createDirectory(BASE_PATH);
		}
		logger.info("BasePath: {}", BASE_PATH);
	}
	
	protected void registerUser(IH2HNode registerNode, UserCredentials credentials) throws NoPeerConnectionException {
		IProcessComponent registerProcess = registerNode.getUserManager().register(credentials);
		TestExecutionUtil.executeProcess(registerProcess);
		logger.info("Registered new user.");
	}
}

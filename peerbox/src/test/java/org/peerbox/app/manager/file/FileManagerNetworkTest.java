package org.peerbox.app.manager.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.node.INodeManager;

public class FileManagerNetworkTest extends BaseJUnitTest {

	private final static int NETWORK_SIZE = 5;
	private static List<IH2HNode> network;
	private static UserCredentials userCredentials;
	private static File uploaderRoot;
	private static IH2HNode uploader;
	private static IH2HNode downloader;

	private INodeManager nodeManager;
	private IFileManager fileManager;

	@Before
	public void setup() throws NoPeerConnectionException, InvalidProcessStateException,
			ProcessExecutionException {
		// setup network
		network = NetworkTestUtil.createH2HNetwork(NETWORK_SIZE);
		// create some random user credentials
		userCredentials = H2HJUnitTest.generateRandomCredentials();
		// register and login a user (peer 0)
		uploaderRoot = FileTestUtil.getTempDirectory();
		uploader = network.get(0);
		uploader.getUserManager()
			.createRegisterProcess(userCredentials).execute();
		uploader.getUserManager()
				.createLoginProcess(userCredentials, new TestFileAgent(uploaderRoot)).execute();

		// other client to verify this
		File downloaderRoot = FileTestUtil.getTempDirectory();
		downloader = network.get(1);
		downloader.getUserManager()
				.createLoginProcess(userCredentials, new TestFileAgent(downloaderRoot)).execute();

		nodeManager = Mockito.mock(INodeManager.class);
		Mockito.stub(nodeManager.getNode()).toReturn(uploader);
		UserConfig userConfig = Mockito.mock(UserConfig.class);
		Mockito.stub(userConfig.getRootPath()).toReturn(uploaderRoot.toPath());
		fileManager = new FileManager(nodeManager, userConfig);
	}

	@After
	public void teardown() {
		if (uploaderRoot != null) {
			FileUtils.deleteQuietly(uploaderRoot);
		}
		NetworkTestUtil.shutdownH2HNetwork(network);
	}

	@Test
	public void testExistsRemote() throws IOException, InvalidProcessStateException,
			ProcessExecutionException, NoPeerConnectionException, NoSessionException,
			IllegalArgumentException {

		Path toUpload = new File(uploaderRoot, "file.txt").toPath();
		Files.write(toUpload, "testData".getBytes());

		assertFalse(fileManager.existsRemote(toUpload));
		fileManager.add(toUpload).execute();
		assertTrue(fileManager.existsRemote(toUpload));
		fileManager.delete(toUpload).execute();
		assertFalse(fileManager.existsRemote(toUpload));
	}

}

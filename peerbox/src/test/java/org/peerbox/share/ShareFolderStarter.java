package org.peerbox.share;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang3.RandomStringUtils;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestFileConfiguration;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.mockito.Mockito;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.app.manager.user.UserManager;
import org.peerbox.events.MessageBus;

public class ShareFolderStarter extends Application {

	private static final int NETWORK_SIZE = 5;
	private static List<IH2HNode> network;


	// clientA
	private IH2HNode[] clientsA = new IH2HNode[2];

	// clientB
	private IH2HNode[] clientsB = new IH2HNode[2];

	private Path basePath;
	private Path rootA;
	private Path rootB;

	private String sharedFolderName = "SharedFolder";

	private ShareFolderController controller;

	static {
		TestFileConfiguration.CHUNK_SIZE = 1024*1024;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		initNetwork();

		initFiles();

		initDownloadSharedFolder();

		initGui();

	}

	private void initNetwork() throws Exception {
		network = NetworkTestUtil.createH2HNetwork(NETWORK_SIZE);

		UserCredentials credentialsA = new UserCredentials("UserA", "PasswordA", "PinA");
		UserCredentials credentialsB = new UserCredentials("UserB", "PasswordB", "PinB");
		clientsA[0] = network.get(0);
		clientsB[0] = network.get(1);
		clientsA[1] = network.get(2);
		clientsB[1] = network.get(3);

		basePath = FileTestUtil.getTempDirectory().toPath();

		// register 2 users and login
		rootA = basePath.resolve("ClientA");
		Files.createDirectory(rootA);
		clientsA[0].getUserManager().createRegisterProcess(credentialsA).execute();
		clientsA[0].getUserManager().createLoginProcess(credentialsA, new TestFileAgent(rootA.toFile())).execute();

		rootB = basePath.resolve("ClientB");
		Files.createDirectory(rootB);
		clientsB[0].getUserManager().createRegisterProcess(credentialsB).execute();
		clientsB[0].getUserManager().createLoginProcess(credentialsB, new TestFileAgent(rootB.toFile())).execute();
	}


	private void initFiles() throws Exception {
		Path toShare = rootA.resolve(sharedFolderName);
		Files.createDirectory(toShare);

		Path f = toShare.resolve("testfile.txt");
		String content = RandomStringUtils.randomAscii(512*1024);
		Files.write(f, content.getBytes());

		clientsA[0].getFileManager().createAddProcess(toShare.toFile()).execute();
		clientsA[0].getFileManager().createAddProcess(f.toFile()).execute();
	}

	private void initGui() {
		MessageBus messageBus = Mockito.mock(MessageBus.class);

		INodeManager nodeManager = Mockito.mock(INodeManager.class);
		Mockito.stub(nodeManager.getNode()).toReturn(clientsA[0]);

		UserConfig userConfig = Mockito.mock(UserConfig.class);
		Mockito.stub(userConfig.getRootPath()).toReturn(rootA);

		IFileManager fileManager = new FileManager(nodeManager, userConfig);

		IUserManager userManager = new UserManager(nodeManager, messageBus);

		controller = new ShareFolderController(fileManager, userManager, messageBus);

		ShareFolderUILoader uiLoader = new ShareFolderUILoader(controller);
		uiLoader.setFolderToShare(rootA.resolve(sharedFolderName));
		uiLoader.loadUi();
	}

	private void initDownloadSharedFolder() throws IOException {
		// download folder as notification is received.
		clientsB[0].getFileManager().subscribeFileEvents(new IFileEventListener() {
			@Handler
			@Override
			public void onFileUpdate(IFileUpdateEvent fileEvent) {

			}

			@Handler
			@Override
			public void onFileShare(IFileShareEvent fileEvent) {
				System.out.println("Invited by '" + fileEvent.getInvitedBy()
						+ "',  shared folder: " + fileEvent.getFile().toString());
				try {
					FileNode root = clientsB[0].getFileManager().createFileListProcess().execute();
					for (FileNode n : FileNode.getNodeList(root, true, true)) {
						if (n.getFile().toPath().startsWith(fileEvent.getFile().toPath())) {
							clientsB[0].getFileManager().createDownloadProcess(n.getFile()).execute();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Handler
			@Override
			public void onFileMove(IFileMoveEvent fileEvent) {

			}

			@Handler
			@Override
			public void onFileDelete(IFileDeleteEvent fileEvent) {

			}

			@Handler
			@Override
			public void onFileAdd(IFileAddEvent fileEvent) {

			}
		});
	}

}

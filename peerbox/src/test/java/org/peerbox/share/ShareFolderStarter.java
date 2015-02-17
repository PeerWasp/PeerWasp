package org.peerbox.share;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.lang3.RandomStringUtils;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.mockito.Mockito;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.app.manager.user.UserManager;
import org.peerbox.events.MessageBus;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.share.ShareFolderController;
import org.peerbox.share.ShareFolderHandler;

import com.google.inject.Injector;

public class ShareFolderStarter extends Application {

	private static final int NETWORK_SIZE = 15;
	private static List<IH2HNode> network;


	// clientA
	private IH2HNode[] clientsA = new IH2HNode[2];

	// clientB
	private IH2HNode[] clientsB = new IH2HNode[2];

	private Path basePath;
	private Path rootA;
	private Path rootB;

	private String sharedFolderName = "SharedFolder";

	private ShareFolderHandler stage;


	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		initNetwork();

		initFiles();

		initGui();

		shareFolder();

	}

	private void initFiles() throws IOException, InvalidProcessStateException, ProcessExecutionException, NoPeerConnectionException, NoSessionException, IllegalArgumentException {
		Path toShare = rootA.resolve(sharedFolderName);
		Files.createDirectory(toShare);

		Path f = toShare.resolve("testfile.txt");
		String content = RandomStringUtils.randomAscii(512*1024);
		Files.write(f, content.getBytes());

		clientsA[0].getFileManager().createAddProcess(toShare.toFile()).execute();
		clientsA[0].getFileManager().createAddProcess(f.toFile()).execute();
	}

	private void shareFolder() throws IOException {
		Path toShare = basePath.resolve("ClientA").resolve(sharedFolderName);

		stage.shareFolder(toShare);

	}

	private void initNetwork() throws IOException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException, ProcessExecutionException {
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


	private void initGui() {
		INodeManager manager = Mockito.mock(INodeManager.class);
		Mockito.stub(manager.getNode()).toReturn(clientsA[0]);
		UserConfig userConfig = Mockito.mock(UserConfig.class);
		MessageBus messageBus = new MessageBus();

		IFileManager fileManager = new FileManager(manager, userConfig, messageBus);
		IUserManager userManager = new UserManager(manager, messageBus);

		stage = new ShareFolderHandler();
		ShareFolderController controller = new ShareFolderController(fileManager, userManager);

		stage.setFxmlLoaderProvider(new IFxmlLoaderProvider() {

			@Override
			public FXMLLoader create(String fxmlFile) {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource(fxmlFile));
				loader.setControllerFactory( new Callback<Class<?>, Object>() {
					@Override
					public Object call(Class<?> param) {
						return controller;
					}
				});
				return loader;
			}

			@Override
			public FXMLLoader create(String fxmlFile, Injector injector) throws IOException {
				return create(fxmlFile);
			}
		});
	}

}

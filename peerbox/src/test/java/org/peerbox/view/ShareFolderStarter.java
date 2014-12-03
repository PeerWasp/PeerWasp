package org.peerbox.view;

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
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.model.UserManager;
import org.peerbox.presenter.ShareFolderController;

public class ShareFolderStarter extends Application {
	
	private static final int networkSize = 6;
	private static List<IH2HNode> network;

	
	// clientA
	private IH2HNode[] clientsA = new IH2HNode[2];
	
	// clientB
	private IH2HNode[] clientsB = new IH2HNode[2];

	private Path basePath;
	private Path rootA;
	private Path rootB;
	
	private String sharedFolderName = "SharedFolder";
	
	private ShareFolderStage stage;
	
	
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

	private void initFiles() throws IOException, NoSessionException, NoPeerConnectionException, IllegalFileLocation, InterruptedException, InvalidProcessStateException {
		Path toShare = rootA.resolve(sharedFolderName);
		Files.createDirectory(toShare);
		
		Path f = toShare.resolve("testfile.txt");
		String content = RandomStringUtils.randomAscii(512*1024);
		Files.write(f, content.getBytes());
		
		clientsA[0].getFileManager().add(toShare.toFile()).start().await();
		clientsA[0].getFileManager().add(f.toFile()).start().await();
	}

	private void shareFolder() throws IOException {
		Path toShare = basePath.resolve("ClientA").resolve(sharedFolderName);
		
		stage.onShareFolderRequested(toShare);
		
	}

	private void initNetwork() throws IOException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		network = NetworkTestUtil.createH2HNetwork(networkSize);
		
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
		clientsA[0].getUserManager().register(credentialsA).start().await();
		clientsA[0].getUserManager().login(credentialsA, new TestFileAgent(rootA.toFile())).start().await();
		
		rootB = basePath.resolve("ClientB");
		Files.createDirectory(rootB);
		clientsB[0].getUserManager().register(credentialsB).start().await();
		clientsB[0].getUserManager().login(credentialsB, new TestFileAgent(rootB.toFile())).start().await();
	
	}
	

	private void initGui() {
		FileManager fileManager = new FileManager(clientsA[0].getFileManager());
		UserManager userManager = new UserManager(clientsA[0].getUserManager());
		
		stage = new ShareFolderStage();
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
		});
	}

}

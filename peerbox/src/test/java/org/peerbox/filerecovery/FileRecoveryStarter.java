package org.peerbox.filerecovery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.hive2hive.core.H2HJUnitTest;
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

public class FileRecoveryStarter extends Application {
	
	private static final int networkSize = 6;
	private static List<IH2HNode> network;

	private IH2HNode client;
	private UserCredentials userCredentials;
	private File root;
	private File file;
	
	private static final int FILE_SIZE = 512*1024;
	private static final int NUM_VERSIONS = 5;
	
	private static List<String> content;
	private static final String fileName = "test-file.txt";
	
	private FileManager fileManager;
	
	private RecoverFileController controller;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		initNetwork();
		
		uploadFileVersions();
		
		initGui();
		
	}

	private void initNetwork() throws IOException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		network = NetworkTestUtil.createH2HNetwork(networkSize);
		userCredentials = H2HJUnitTest.generateRandomCredentials();
		client = network.get(RandomUtils.nextInt(0, network.size()));;
	
		// register a user
		root = FileTestUtil.getTempDirectory();
		client.getUserManager().register(userCredentials).start().await();
		client.getUserManager().login(userCredentials, new TestFileAgent(root)).start().await();
	}

	private void uploadFileVersions() throws IOException, NoSessionException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException, IllegalFileLocation {
		content = new ArrayList<String>();
		
		// add an intial file to the network
		file = new File(root, fileName);
		String fileContent = RandomStringUtils.randomAscii(FILE_SIZE);
		content.add(fileContent);
		FileUtils.write(file, fileContent);
		client.getFileManager().add(file).start().await();
	
		// update and upload 
		for(int i = 0; i < NUM_VERSIONS; ++i) {
			Thread.sleep(2000); // sleep such that each file has different timestamp
			fileContent = RandomStringUtils.randomAscii(FILE_SIZE);
			content.add(fileContent);
			FileUtils.write(file, fileContent);
			client.getFileManager().update(file).start().await();
		}
	}

	private void initGui() {
		
		fileManager = new FileManager(client.getFileManager());
		
		FileRecoveryHandler stage = new FileRecoveryHandler();
		controller = new RecoverFileController();
		controller.setFileManager(fileManager);
		
		stage.setFxmlLoaderProvider(new IFxmlLoaderProvider() {
			
			@Override
			public FXMLLoader create(String fxmlFile) {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource(fxmlFile));
				loader.setControllerFactory( new Callback<Class<?>, Object>() {
					@Override
					public Object call(Class<?> param) {
						controller.setFileManager(fileManager);
						return controller;
					}
				});
				return loader;
			}
		});
		
		stage.recoverFile(Paths.get(root.toString(), fileName));
		
	}

	

}

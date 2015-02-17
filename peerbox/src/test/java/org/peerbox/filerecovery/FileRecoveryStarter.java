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
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.mockito.Mockito;
import org.peerbox.app.AppContext;
import org.peerbox.app.ClientContext;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.app.manager.user.UserManager;
import org.peerbox.events.MessageBus;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.testutils.NetworkTestUtil;

import com.google.inject.Injector;

public class FileRecoveryStarter extends Application {

	private static final int NETWORK_SIZE = 15;
	private static List<INodeManager> network;

	private INodeManager nodeManager;
	private IUserManager userManager;
	private IFileManager fileManager;

	private UserConfig userConfig;
	private MessageBus messageBus;


	private UserCredentials userCredentials;
	private File root;
	private File file;

	private static final int FILE_SIZE = 1024;
	private static final double INCREASE_FACTOR = 1.5;
	private static final int NUM_VERSIONS = 5;

	private static List<String> content;
	private static final String fileName = "test-file.txt";



	private RecoverFileController controller;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		messageBus = Mockito.mock(MessageBus.class);

		initNetwork();

		uploadFileVersions();

		initGui();

	}

	private void initNetwork() throws NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		// setup network and node
		network = NetworkTestUtil.createNetwork(NETWORK_SIZE);
		userCredentials = H2HJUnitTest.generateRandomCredentials();
		nodeManager = network.get(RandomUtils.nextInt(0, network.size()));;

		// user config
		root = FileTestUtil.getTempDirectory();
		userConfig = Mockito.mock(UserConfig.class);
		Mockito.stub(userConfig.getRootPath()).toReturn(root.toPath());

		// register and login
		userManager = new UserManager(nodeManager, messageBus);
		userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root.toPath());
	}

	private void uploadFileVersions() throws IOException, NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException, IllegalArgumentException, InterruptedException {
		fileManager = new FileManager(nodeManager, userConfig, messageBus);

		content = new ArrayList<String>();

		// add an intial file to the network
		file = new File(root, fileName);
		String fileContent = RandomStringUtils.randomAscii(FILE_SIZE);
		content.add(fileContent);
		FileUtils.write(file, fileContent);
		nodeManager.getNode().getFileManager().createAddProcess(file).execute();

		// update and upload
		int fileSize = FILE_SIZE;
		for(int i = 0; i < NUM_VERSIONS; ++i) {
			Thread.sleep(2000); // sleep such that each file has different timestamp
			fileSize *= INCREASE_FACTOR;
			fileContent = RandomStringUtils.randomAscii(fileSize);
			content.add(fileContent);
			FileUtils.write(file, fileContent);
			nodeManager.getNode().getFileManager().createUpdateProcess(file).execute();
		}
	}

	private void initGui() {

		controller = new RecoverFileController(fileManager);

		ClientContext clientContext = new ClientContext();
		clientContext.setNodeManager(nodeManager);
		clientContext.setUserManager(userManager);
		clientContext.setFileManager(fileManager);
		clientContext.setUserConfig(userConfig);
		clientContext.setInjector(Mockito.mock(Injector.class));


		AppContext appContext = Mockito.mock(AppContext.class);
		Mockito.stub(appContext.getCurrentClientContext()).toReturn(clientContext);

		// recovery
		FileRecoveryHandler handler = new FileRecoveryHandler();
		handler.setAppContext(appContext);

		// fxml GUI loading and controller wiring
		FileRecoveryUILoader uiLoader = new FileRecoveryUILoader();
		uiLoader.setFxmlLoaderProvider(new IFxmlLoaderProvider() {
			@Override
			public FXMLLoader create(String fxmlFile) throws IOException {
				return create(fxmlFile, null);
			}

			@Override
			public FXMLLoader create(String fxmlFile, Injector injector) throws IOException {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource(fxmlFile));
				loader.setControllerFactory(new Callback<Class<?>, Object>() {
					@Override
					public Object call(Class<?> param) {
						return controller;
					}
				});
				return loader;
			}
		});
		Mockito.doReturn(uiLoader).when(clientContext.getInjector()).getInstance(FileRecoveryUILoader.class);

		// start recovery procedure
		handler.recoverFile(Paths.get(root.toString(), fileName));

	}

}

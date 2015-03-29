package org.peerbox.filerecovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestFileConfiguration;
import org.hive2hive.core.utils.TestProcessComponentListener;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.BaseJUnitTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileVersionSelectorTest extends BaseJUnitTest {

	private static Logger logger = LoggerFactory.getLogger(FileVersionSelectorTest.class);

	private static final int NETWORK_SIZE = 6;
	private static List<IH2HNode> network;

	private static IH2HNode client;
	private static UserCredentials userCredentials;
	private static File root;
	private static File file;

	private static int FILE_SIZE = 128*1024;
	private static int NUM_VERSIONS = 4;

	private static List<String> content;
	private static final String fileName = "test-file.txt";


	@BeforeClass
	public static void beforeClass() throws Exception {
		initNetwork();
		uploadVersions();

		// keep at least (num versions + initial version) many versions
		assertTrue(NUM_VERSIONS <= new TestFileConfiguration().getMaxNumOfVersions()-1);
	}

	private static void initNetwork() throws InvalidProcessStateException,
			NoPeerConnectionException, ProcessExecutionException {
		network = NetworkTestUtil.createH2HNetwork(NETWORK_SIZE);
		client = network.get(RandomUtils.nextInt(0, network.size()));

		// register a user
		userCredentials = H2HJUnitTest.generateRandomCredentials();
		root = FileTestUtil.getTempDirectory();
		client.getUserManager().createRegisterProcess(userCredentials).execute();
		client.getUserManager().createLoginProcess(userCredentials, new TestFileAgent(root)).execute();
	}

	private static void uploadVersions() throws  Exception {
		content = new ArrayList<String>();

		// add an intial file to the network
		file = new File(root, fileName);
		String fileContent = RandomStringUtils.randomAscii(FILE_SIZE);
		content.add(fileContent);
		logger.info("Initial content: {}...", fileContent.substring(0, 10));
		FileUtils.write(file, fileContent);
		client.getFileManager().createAddProcess(file).execute();

		// update and upload
		for(int i = 0; i < NUM_VERSIONS; ++i) {
			Thread.sleep(2000); // sleep such that each file has different timestamp
			fileContent = RandomStringUtils.randomAscii(FILE_SIZE);
			content.add(fileContent);
			logger.info("File version {} content: {}...", i, fileContent.substring(0, 10));
			FileUtils.write(file, fileContent);
			client.getFileManager().createUpdateProcess(file).execute();
		}
	}

	@AfterClass
	public static void afterClass() {
		NetworkTestUtil.shutdownH2HNetwork(network);
	}

	@Test
	public void testRecoverAllVersions() throws Exception {
		// recover all versions
		for(int i = 0; i < NUM_VERSIONS; ++ i) {
			recoverVersion(i);
		}
	}

	private FileVersionSelectorListener recoverVersion(int version) throws Exception {
		// recover version
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(file.toPath(), version);
		client.getFileManager().createRecoverProcess(file, versionSelectorListener.getFileVersionSelector()).execute();

		// assert content equality
		String recoveredFileName = versionSelectorListener.getRecoveredFileName();
		assertNotNull(recoveredFileName);
		assertFalse(recoveredFileName.isEmpty());

		Path recoveredFile = Paths.get(root.toString(), recoveredFileName);
		assertTrue(Files.exists(recoveredFile));

		String expected = content.get(version);
		String recovered = new String(Files.readAllBytes(recoveredFile));
		logger.info("Version {}:\n\tExpected content: {}... \n\tRecovered content: {}...",
				version, expected.substring(0, 10), recovered.substring(0, 10));
		assertTrue(expected.equals(recovered));

		return versionSelectorListener;
	}

	@Test
	public void testCancel() throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		// count number of files to make sure no file recovered
		int numElementsBefore = root.list().length;

		// recover version and cancel
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(file.toPath(), -1);
		try {
			client.getFileManager().createRecoverProcess(file, versionSelectorListener.getFileVersionSelector()).execute();
			fail("Expected exception was not thrown.");
		} catch(ProcessExecutionException pex) {
			// expected exception since no version selected when cancelled.
			logger.info("Exception: {}", pex.getMessage());
		}

		int numElementsAfter = root.list().length;
		assertEquals(numElementsBefore, numElementsAfter);
	}

	@Test
	public void testCancelBeforeSelect() throws NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		// recover version and cancel
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(file.toPath(), 0);
		versionSelectorListener.getFileVersionSelector().cancel(); // cancel before onAvailableVersionsReceived
		IProcessComponent<Void> p = client.getFileManager().createRecoverProcess(file, versionSelectorListener.getFileVersionSelector());
		TestProcessComponentListener plistener = new TestProcessComponentListener();
		p.attachListener(plistener);
		try {
			p.execute();
			fail("Expected exception was not thrown.");
		} catch (ProcessExecutionException pex) {
			// expected exception since no version selected when cancelled.
			logger.info("Exception: {}", pex.getMessage());
		}
		assertTrue(plistener.hasExecutionFailed());
		assertFalse(plistener.hasExecutionSucceeded());
	}

	@Test(expected=IllegalStateException.class)
	public void testSelectBeforeOnAvailableVersions() {
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(file.toPath(), 0);
		versionSelectorListener.getFileVersionSelector().selectVersion((IFileVersion)null, file.toPath());
	}

	@Test(expected=IllegalStateException.class)
	public void testSelectTwice() throws FileNotFoundException, NoSessionException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(file.toPath(), 0);
		List<IFileVersion> versions = new ArrayList<IFileVersion>();
		for(int i = 0; i < NUM_VERSIONS; ++i) {
			versions.add(null);
		}
		versionSelectorListener.getFileVersionSelector().selectVersion(versions);
		versionSelectorListener.getFileVersionSelector().selectVersion((IFileVersion)null, file.toPath());
	}

	@Test
	public void testRecoverMultipleTimes() throws Exception {
		// if same version is recovered multiple times, the file name should have a counter
		FileVersionSelectorListener listener_1 = recoverVersion(0);
		FileVersionSelectorListener listener_2 = recoverVersion(0);

		String recoveredName_1 = listener_1.getRecoveredFileName();
		String recoveredName_2 = listener_2.getRecoveredFileName();
		assertFalse(recoveredName_1.equals(recoveredName_2));
	}


	private class FileVersionSelectorListener implements IFileVersionSelectorListener {

		private Path fileToRecover;
		private int versionToRecover;
		private FileVersionSelector versionSelector;


		public FileVersionSelectorListener(Path fileToRecover, int versionToRecover) {
			this.fileToRecover = fileToRecover;
			this.versionToRecover = versionToRecover;
			this.versionSelector = new FileVersionSelector(this);

		}

		public String getRecoveredFileName() {
			return versionSelector.getRecoveredFileName();
		}

		public FileVersionSelector getFileVersionSelector() {
			return versionSelector;
		}

		@Override
		public void onAvailableVersionsReceived(List<IFileVersion> availableVersions) {
			Assert.assertTrue(availableVersions.size() == NUM_VERSIONS);
			Assert.assertTrue(versionToRecover < availableVersions.size());

			if(versionToRecover != -1) {
				versionSelector.selectVersion(availableVersions.get(versionToRecover), fileToRecover);
			} else {
				versionSelector.cancel();
			}
		}
	}

}

package org.peerbox;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.concretes.ProcessComponentListener;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.interfaces.IFileVersionSelectorEventListener;
import org.peerbox.presenter.FileVersionSelector;

public class FileVersionSelectorTest {
	
	private static final int networkSize = 6;
	private static List<IH2HNode> network;

	private static IH2HNode client;
	private static UserCredentials userCredentials;
	private static File root;
	private static File file;
	
	private static int FILE_SIZE = 512*1024;
	private static int NUM_VERSIONS = 5;
	
	private static List<String> content;
	private static final String fileName = "test-file.txt";
	
	
	@BeforeClass
	public static void beforeClass() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, IOException, NoSessionException, IllegalFileLocation {
		initNetwork();
		uploadVersions();
	}

	private static void initNetwork() throws InterruptedException, InvalidProcessStateException,
			NoPeerConnectionException {
		network = NetworkTestUtil.createH2HNetwork(networkSize);
		client = network.get(RandomUtils.nextInt(0, network.size()));
	
		// register a user
		userCredentials = H2HJUnitTest.generateRandomCredentials();
		root = FileTestUtil.getTempDirectory();
		client.getUserManager().register(userCredentials).start().await();
		client.getUserManager().login(userCredentials, new TestFileAgent(root)).start().await();
	}

	private static void uploadVersions() throws IOException, InterruptedException,
			InvalidProcessStateException, NoSessionException, NoPeerConnectionException,
			IllegalFileLocation {
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
	
	@AfterClass
	public static void afterClass() {
		NetworkTestUtil.shutdownH2HNetwork(network);
	}
	
	@Test
	public void testRecoverAllVersions() throws NoSessionException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException, IOException {
		
		// recover all versions
		for(int i = 0; i < NUM_VERSIONS; ++ i) {
			
			// recover version
			FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(i);
			client.getFileManager().recover(file, versionSelectorListener.getFileVersionSelector()).start().await();
			
			// assert content equality
			String recoveredFileName = versionSelectorListener.getRecoveredFileName();
			assertNotNull(recoveredFileName);
			assertFalse(recoveredFileName.isEmpty());
			
			Path recoveredFile = Paths.get(root.toString(), recoveredFileName);
			assertTrue(Files.exists(recoveredFile));
			
			String expected = content.get(i);
			String recovered = new String(Files.readAllBytes(recoveredFile));
			assertTrue(expected.equals(recovered));
		}
	}
	
	@Test
	public void testCancel() throws FileNotFoundException, NoSessionException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// count number of files to make sure no file recovered
		int numElementsBefore = root.list().length;
		
		// recover version and cancel
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(-1);
		client.getFileManager().recover(file, versionSelectorListener.getFileVersionSelector()).start().await();
	
		int numElementsAfter = root.list().length;
		assertEquals(numElementsBefore, numElementsAfter);
	}
	
	@Test
	public void testCancelBeforeSelect() throws FileNotFoundException, NoSessionException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// recover version and cancel
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(0);
		versionSelectorListener.getFileVersionSelector().cancel(); // cancel before onAvailableVersionsReceived
		IProcessComponent p = client.getFileManager().recover(file, versionSelectorListener.getFileVersionSelector());
		ProcessComponentListener plistener = new ProcessComponentListener();
		p.attachListener(plistener);
		p.start().await();
		
		assertTrue(plistener.hasFinished());
		assertTrue(plistener.hasFailed());
		assertFalse(plistener.hasSucceeded());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSelectBeforeOnAvailableVersions() {
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(0);
		versionSelectorListener.getFileVersionSelector().selectVersion((IFileVersion)null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSelectTwice() throws FileNotFoundException, NoSessionException, NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		FileVersionSelectorListener versionSelectorListener = new FileVersionSelectorListener(0);
		List<IFileVersion> versions = new ArrayList<IFileVersion>();
		for(int i = 0; i < NUM_VERSIONS; ++i) {
			versions.add(null);
		}
		versionSelectorListener.getFileVersionSelector().selectVersion(versions);
		versionSelectorListener.getFileVersionSelector().selectVersion((IFileVersion)null);
	}
	
	
	private class FileVersionSelectorListener implements IFileVersionSelectorEventListener {

		private FileVersionSelector versionSelector;
		private int versionToRecover;
		
		
		public FileVersionSelectorListener(int versionToRecover) {
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
				versionSelector.selectVersion(availableVersions.get(versionToRecover));
			} else {
				versionSelector.cancel();
			}
		}
	}
	
}

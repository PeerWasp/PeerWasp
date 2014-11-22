package org.peerbox.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.peerbox.presenter.validation.SelectRootPathUtils;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

public class H2HManagerTest {
	
	private String path;
	private H2HManager h2hManager;
	
	@Before
	public void initializeVariables(){
		path = System.getProperty("user.dir").replace("\\", "/") + "/"; 
		h2hManager = new H2HManager();
	}
	
	@Test(expected=UnknownHostException.class)
	public void accessNetworkTestUnknownHost() throws UnknownHostException{
		h2hManager.createNode();
		h2hManager.joinNetwork("unknownhost");
	}
	
	@Test
	public void accessNetworkTestWrongBootstrapAddress() throws UnknownHostException{
		h2hManager.createNode();
		assertFalse(h2hManager.joinNetwork("1.2.3.4"));
	}
	
	@Test
	public void accessNetworkTestCorrectBootstrapAddress() throws UnknownHostException{
		h2hManager.createNode();
		assertTrue(h2hManager.joinNetwork("localhost"));
	}
	
	@Test
	public void getInetAddressAsStringIfInitialPeer(){
		try {
			h2hManager.createNode();
			String address = h2hManager.getInetAddressAsString();
			System.out.println(address);
			String localhost = InetAddress.getLocalHost().getHostAddress();
			assertEquals(address, localhost);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void getInetAddressAsStringIfNotInitialPeer(){
		String nodeID = h2hManager.generateNodeID();
		String bootstrapAddress = "1.2.3.4";
		try {
			h2hManager.createNode(NetworkConfiguration.create(nodeID, InetAddress.getByName(bootstrapAddress)));
			String address = h2hManager.getInetAddressAsString();
			assertEquals(address, bootstrapAddress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRootPathButSelectFile() throws IOException {
		File testFile = new File("Test.txt");
		assertTrue(testFile.createNewFile());

		ValidationResult res = SelectRootPathUtils.validateRootPath(Paths.get("Test.txt"));
		assertEquals(res, ValidationResult.ROOTPATH_NOTADIRECTORY);

		assertTrue(testFile.delete());
	}
	
	@Test
	public void testRootPathWithExistingDirectory() throws IOException {
		File testDir = new File("Test_ExistingDir");
		assertTrue(testDir.mkdir());
		
		ValidationResult res = SelectRootPathUtils.validateRootPath(Paths.get("Test_ExistingDir"));
		assertEquals(res, ValidationResult.OK);
		
		assertTrue(testDir.delete());
	}
	
	@Test
	public void testRootPathWithNewDirectory() throws IOException {
		// TODO: refactoring required -- cannot have user interaction (dialog asking user whether to
		// create directory or not) here. should be somewhere else...
		ValidationResult res = SelectRootPathUtils.validateRootPath(Paths.get(path, "Test_NewDir"));
		
		assertEquals(res, ValidationResult.OK);
		
		File newDir = new File(path + "Test_NewDir");
		assertTrue(newDir.exists());
		assertTrue(newDir.isDirectory());
		newDir.delete();
	}
	
	@Test
	public void testRootPathWithoutParentDir() throws IOException {
		// TODO: same as above!
		Path p = Paths.get(path, "doesnotexist/Test_Dir");
		ValidationResult res = SelectRootPathUtils.validateRootPath(p);
		assertEquals(res, ValidationResult.OK);
		
		Files.delete(p);
		
	}
}

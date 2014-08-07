package org.peerbox.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.junit.Before;
import org.junit.Test;
import org.peerbox.presenter.SelectRootPathUtils;

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
		h2hManager.accessNetwork("unknownhost");
	}
	
	@Test
	public void accessNetworkTestWrongBootstrapAddress() throws UnknownHostException{
		h2hManager.createNode();
		assertFalse(h2hManager.accessNetwork("1.2.3.4"));
	}
	
	@Test
	public void accessNetworkTestCorrectBootstrapAddress() throws UnknownHostException{
		h2hManager.createNode();
		assertTrue(h2hManager.accessNetwork("localhost"));
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
	
	@Test(expected=IOException.class)
	public void initializeRootDirectoryButSelectFile() throws IOException{
		File testFile = new File("Test.txt");
		testFile.createNewFile();
		SelectRootPathUtils.initializeRootDirectory("Test.txt");
		
	}
	
	@Test
	public void initializeRootDirectoryWithExistingDirectory() throws IOException{
		File testDir = new File("Test_ExistingDir");
		testDir.mkdir();
		SelectRootPathUtils.initializeRootDirectory("Test_ExistingDir");
		testDir.delete();
	}
	
	@Test
	public void initializeRootDirectoryWithNewDirectory() throws IOException{
		boolean isSuccessfull = SelectRootPathUtils.initializeRootDirectory(path + "Test_NewDir");
		assertTrue(isSuccessfull);
		File newDir = new File(path + "Test_NewDir");
		assertTrue(newDir.exists());
		assertTrue(newDir.isDirectory());
		newDir.delete();
	}
	
	@Test
	public void initializeRootDirectoryWithoutParentDir() throws IOException{
		boolean isSuccessfull = SelectRootPathUtils.initializeRootDirectory(path + "doesnotexist/Test_Dir");
		assertFalse(isSuccessfull);
	}
}

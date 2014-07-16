package org.peerbox.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.junit.Test;

public class H2HManagerTest {
	
	@Test(expected=UnknownHostException.class)
	public void accessNetworkTestUnknownHost() throws UnknownHostException{
		H2HManager.INSTANCE.createNode();
		H2HManager.INSTANCE.accessNetwork("unknownhost");
	}
	
	@Test
	public void accessNetworkTestWrongBootstrapAddress() throws UnknownHostException{
		H2HManager.INSTANCE.createNode();
		assertFalse(H2HManager.INSTANCE.accessNetwork("1.2.3.4"));
	}
	
	@Test
	public void accessNetworkTestCorrectBootstrapAddress() throws UnknownHostException{
		H2HManager.INSTANCE.createNode();
		assertTrue(H2HManager.INSTANCE.accessNetwork("localhost"));
	}
	
	@Test
	public void getInetAddressAsStringIfInitialPeer(){
		try {
			H2HManager.INSTANCE.createNode();
			String address = H2HManager.INSTANCE.getInetAddressAsString();
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
		String nodeID = H2HManager.INSTANCE.generateNodeID();
		String bootstrapAddress = "1.2.3.4";
		try {
			H2HManager.INSTANCE.createNode(NetworkConfiguration.create(nodeID, InetAddress.getByName(bootstrapAddress)));
			String address = H2HManager.INSTANCE.getInetAddressAsString();
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
		H2HManager.INSTANCE.initializeRootDirectory("Test.txt");
		
	}
	
	@Test
	public void initializeRootDirectoryWithExistingDirectory() throws IOException{
		File testDir = new File("Test_ExistingDir");
		testDir.mkdir();
		H2HManager.INSTANCE.initializeRootDirectory("Test_ExistingDir");
		testDir.delete();
	}
	
	@Test
	public void initializeRootDirectoryWithNewDirectory() throws IOException{
		H2HManager.INSTANCE.initializeRootDirectory("Test_NewDir");
		File newDir = new File("Test_NewDir");
		assertTrue(newDir.exists());
		assertTrue(newDir.isDirectory());
		newDir.delete();
	}
}

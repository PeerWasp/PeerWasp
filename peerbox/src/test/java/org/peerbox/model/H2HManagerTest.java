package org.peerbox.model;

import static org.junit.Assert.*;

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
}

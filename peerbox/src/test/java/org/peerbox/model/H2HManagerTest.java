package org.peerbox.model;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
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

}

package org.peerbox.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.Test;
import org.peerbox.utils.OsUtils;
import org.peerbox.utils.WinRegistryTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerTest.class);
	
	@Test
	public void testCreateServer() {
		IServer srv = ServerFactory.createServer();
		assertTrue(srv.getPort() >= 1);
		assertTrue(srv.getPort() <= 65535);	
	}
	
	@Test
	public void testWinRegistryPortUpdate() {
		if(OsUtils.isWindows()) {
			IServer srv = ServerFactory.createServer();
			WinRegistryTest.assertPort(srv.getPort());
		} else {
			fail("Not on Windows.");
		}
	}
	
	@Test
	public void testServerStart() throws IOException {
		IServer srv = ServerFactory.createServer();
		assertTrue(srv.start());
		
		boolean listen = checkListeningOnLocalhost(srv, true);
		assertTrue(listen);
		
		assertTrue(srv.stop());
	}
	
	@Test 
	public void testServerStop() throws InterruptedException, IOException {
		// assumes that starting works...
		boolean listen = false;
		IServer srv = ServerFactory.createServer();
		assertTrue(srv.start());

		listen = checkListeningOnLocalhost(srv, true);
		assertTrue(listen);
		
		assertTrue(srv.stop());
		
		listen = checkListeningOnLocalhost(srv, false);
		assertFalse(listen);
		
	}

	private boolean checkListeningOnLocalhost(IServer srv, boolean shouldListen) throws IOException {
		boolean connectedToLocalhost = false;
		
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
	        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
	        	boolean connected = false;
	        	Socket echoSocket = null;
	        	try {
		        	echoSocket = new Socket(inetAddress, srv.getPort());
		        	connected = echoSocket.isConnected();
	        	} catch (IOException ex) {
	        		logger.debug("{} - {}", inetAddress.toString(), ex.getMessage());
	        		connected = false;
	        	} finally {
	        		
	        		if(inetAddress.equals(InetAddress.getByName("localhost"))) {
	        			logger.debug("{} - connected", inetAddress);
	        			assertTrue(connected == shouldListen);
	        			connectedToLocalhost = connected;
	        		} else {
	        			assertFalse(connected);
	        		}

	        		if(echoSocket != null) {
	        			echoSocket.close();
	        		}
	        	}
	        }
		}
		
		return connectedToLocalhost;
	}
}

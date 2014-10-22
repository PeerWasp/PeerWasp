package org.peerbox.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinRegistry {
	
	private static final Logger logger = LoggerFactory.getLogger(WinRegistry.class);
	
	public static boolean setApiServerPort(int port) {
		if(port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port out of range (port is: " + 65535 + ")");
		}
		
		ProcessBuilder builder = new ProcessBuilder();
		builder.command(
				"reg", /* registry command */
				"ADD", /* add a new key */ 
				"HKCU\\Software\\PeerBox", /* base key */ 
				"/v api_server_port", /* name of the value */
				"/t REG_DWORD", /* type of the value */ 
				"/d %d", /* actual data */
				"/f" /* force overwrite if key exists */
				);
		
		if(!executeCommand(builder)) {
			logger.warn("Could not set the port in the registry");
			return false;
		}
		return true;
	}
	
	
	private static boolean executeCommand(ProcessBuilder builder) {
		try {
			if(!builder.start().waitFor(1, TimeUnit.SECONDS)) {
				return false;
			}
		} catch (InterruptedException | IOException e) {
			logger.info("Exception during command execution", e);
			return false;
		}
		return true;
	}
	
}

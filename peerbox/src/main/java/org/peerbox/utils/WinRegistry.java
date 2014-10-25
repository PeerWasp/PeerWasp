package org.peerbox.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the Windows Registry.
 * 
 * @author albrecht
 *
 */
public class WinRegistry {

	private static final Logger logger = LoggerFactory.getLogger(WinRegistry.class);

	/**
	 * Set the api_server_port in the registry
	 * 
	 * @param port
	 * @return true if successful
	 */
	public static boolean setApiServerPort(int port) {
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port out of range (port is: " + 65535 + ")");
		}

		ProcessBuilder builder = new ProcessBuilder();

		builder.command("reg", /* registry command */
				"ADD", /* add a new key */
				"HKCU\\Software\\PeerBox", /* registry key */
				"/v", "api_server_port", /* name of the value */
				"/t", "REG_DWORD", /* type of the value */
				"/d", String.format("%d", port), /* actual data */
				"/f" /* force overwrite if key exists */
		);

		if (!ExecuteProcessUtils.executeCommand(builder, null)) {
			logger.warn("Could not set the port in the registry");
			return false;
		}
		return true;
	}

	/**
	 * Set the rootpath in the registry
	 * 
	 * @param rootpath
	 * @return true if successful
	 */
	public static boolean setRootPath(String rootPath) {
		if (rootPath == null) {
			throw new IllegalArgumentException("rootPath cannot be null");
		}

		ProcessBuilder builder = new ProcessBuilder();

		builder.command("reg", /* registry command */
				"ADD", /* add a new key */
				"HKCU\\Software\\PeerBox", /* registry key */
				"/v", "rootpath", /* name of the value */
				"/t", "REG_SZ", /* type of the value */
				"/d", rootPath, /* actual data */
				"/f" /* force overwrite if key exists */
		);

		if (!ExecuteProcessUtils.executeCommand(builder, null)) {
			logger.warn("Could not set the port in the registry");
			return false;
		}
		return true;
	}

}

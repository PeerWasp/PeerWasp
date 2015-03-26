package org.peerbox.utils;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the Windows Registry.
 *
 * @author albrecht
 *
 */
public final class WinRegistry {

	private static final Logger logger = LoggerFactory.getLogger(WinRegistry.class);

	private WinRegistry() {
		// prevent instances
	}

	/**
	 * Set the api_server_port in the registry
	 *
	 * @param port on which server is listening
	 * @return true if successful
	 */
	public static boolean setApiServerPort(int port) {
		if (!NetUtils.isValidPort(port)) {
			throw new IllegalArgumentException("Port out of range (port is: " + port + ")");
		}
		if (!OsUtils.isWindows()) {
			throw new RuntimeException("Cannot set ApiServerPort in registry (not running on Windows).");
		}

		ProcessBuilder builder = new ProcessBuilder();

		builder.command("reg", /* registry command */
				"ADD", /* add a new key */
				"HKCU\\Software\\PeerWasp", /* registry key */
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
	 * @param rootPath of the user
	 * @return true if successful
	 */
	public static boolean setRootPath(Path rootPath) {
		if (rootPath == null) {
			throw new IllegalArgumentException("rootPath cannot be null");
		}
		if (!OsUtils.isWindows()) {
			throw new RuntimeException("Cannot set RootPath in registry (not running on Windows).");
		}

		ProcessBuilder builder = new ProcessBuilder();

		builder.command("reg", /* registry command */
				"ADD", /* add a new key */
				"HKCU\\Software\\PeerWasp", /* registry key */
				"/v", "rootpath", /* name of the value */
				"/t", "REG_SZ", /* type of the value */
				"/d", rootPath.toString(), /* actual data */
				"/f" /* force overwrite if key exists */
		);

		if (!ExecuteProcessUtils.executeCommand(builder, null)) {
			logger.warn("Could not set the port in the registry");
			return false;
		}
		return true;
	}

}

package org.peerbox.server;

import org.hive2hive.core.network.NetworkUtils;
import org.peerbox.utils.OsUtils;
import org.peerbox.utils.WinRegistry;

/**
 * Factory that creates and initializes a new server instance.
 * 
 * @author albrecht
 *
 */
public class ServerFactory {

	// port range in which we search a free port
	private static final int MAX_PORT = 65535;
	private static final int MIN_PORT = 30000;
	
	// base path in the URL of the service for context menu
	private static final String CONTEXT_MENU_PATH_TEMPLATE = "/contextmenu/%s";

	/**
	 * Creates a new server instance
	 * 
	 * @return server
	 */
	public static IServer createServer() {
		// get free port
		int port = getFreePort();
		if (port <= 0 || port >= 65535) {
			throw new IllegalStateException("Could not find a free port.");
		}

		// update registry
		if (OsUtils.isWindows()) {
			WinRegistry.setApiServerPort(port);
		}

		HttpServer server = new HttpServer(port);
		return server;
	}


	/**
	 * Returns a free (unused) port
	 * 
	 * @return free port if found, 0 otherwise
	 */
	private static int getFreePort() {
		for (int p = MIN_PORT; p < MAX_PORT; ++p) {
			if (NetworkUtils.isPortAvailable(p)) {
				return p;
			}
		}
		return 0;
	}

	public static String getContextMenuDeletePath() {
		return getContextMenuPath("delete"); 
	}

	public static String getContextMenuVersionsPath() {
		return getContextMenuPath("versions");
	}

	public static String getContextMenuSharePath() {
		return getContextMenuPath("share");
	}
	
	private static String getContextMenuPath(String command) {
		return String.format(ServerFactory.CONTEXT_MENU_PATH_TEMPLATE, command);
	}
}

package org.peerbox.utils;


/**
 * Network utility class
 *
 * @author albrecht
 *
 */
public final class NetUtils {

	// valid port range
	public static final int MIN_PORT = 1;
	public static final int MAX_PORT = 65535;

	private NetUtils() {
		// no instantiation, only static content
	}

	/**
	 * Checks whether a given port is in the valid port range [1, 65535].
	 *
	 * @param port to check
	 * @return true if port is in the valid port range, false otherwise.
	 */
	public static boolean isValidPort(int port) {
		return port >= MIN_PORT && port <= MAX_PORT;
	}
}

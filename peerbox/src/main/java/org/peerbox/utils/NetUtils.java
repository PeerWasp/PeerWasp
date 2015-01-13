package org.peerbox.utils;

public class NetUtils {

	// valid port range
	public static final int MIN_PORT = 1;
	public static final int MAX_PORT = 65535;

	private NetUtils() {
		// no instantiation, only static content
	}

	public static boolean isValidPort(int port) {
		return port >= MIN_PORT && port <= MAX_PORT;
	}
}

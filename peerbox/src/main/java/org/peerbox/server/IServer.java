package org.peerbox.server;

/**
 * Server interface
 * @author albrecht
 *
 */
public interface IServer {

	/**
	 * Starts the server
	 * @return true if successful, false otherwise.
	 */
	boolean start();

	/**
	 * Stops the server
	 * @return true if successful, false otherwise
	 */
	boolean stop();

	/**
	 * Returns the port that this server binds to
	 * 
	 * @return port
	 */
	int getPort();
}

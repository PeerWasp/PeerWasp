package org.peerbox.server;

/**
 * Server interface
 * @author albrecht
 *
 */
public interface IServer {

	/**
	 * Starts the server
	 */
	void start();

	/**
	 * Stops the server
	 */
	void stop();

	/**
	 * Returns the port that this server binds to
	 * 
	 * @return port
	 */
	int getPort();
}

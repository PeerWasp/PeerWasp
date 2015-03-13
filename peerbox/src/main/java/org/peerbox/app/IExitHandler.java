package org.peerbox.app;

public interface IExitHandler {
	/**
	 * Quit the application.
	 * Shuts down services and disconnects from network.
	 */
	void exit();
}

package org.peerbox.server;

public interface IServer {
	void start();
	void stop();
	int getPort();
}

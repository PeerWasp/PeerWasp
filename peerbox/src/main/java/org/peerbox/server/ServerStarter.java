package org.peerbox.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStarter {
	private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

	public static void main(String[] args) throws Exception {

		IServer cmdServer = ServerFactory.createServer();
		cmdServer.start();

		// do not exit and wait
		Thread.currentThread().join();
	}
}

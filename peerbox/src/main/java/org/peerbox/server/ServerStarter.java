package org.peerbox.server;


public class ServerStarter {
	public static void main(String[] args) throws Exception {
		
		IServer cmdServer = ServerFactory.createServer();
		cmdServer.start();
		
		// do not exit here
		Thread.currentThread().join();
	}
}

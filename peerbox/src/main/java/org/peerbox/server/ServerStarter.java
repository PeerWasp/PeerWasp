package org.peerbox.server;

import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.peerbox.utils.WinRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStarter {
	private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

	public static void main(String[] args) throws Exception {
		
		WinRegistry.setRootPath(Paths.get(FileUtils.getUserDirectoryPath(), "PeerBox"));
		IServer cmdServer = ServerFactory.createServer();
		cmdServer.start();

		// do not exit and wait
		Thread.currentThread().join();
	}
}

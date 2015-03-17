package org.peerbox.server;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.peerbox.app.Constants;
import org.peerbox.guice.ApiServerModule;
import org.peerbox.server.IServer;
import org.peerbox.server.helper.ApiServerTestModule;
import org.peerbox.utils.OsUtils;
import org.peerbox.utils.WinRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Starts the webserver and keeps it running.
 * Useful during development and for debugging purposes of the context menu.
 *
 * @author albrecht
 *
 */
public class ServerStarter {
	private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

	public static void main(String[] args) throws Exception {

		Path rootPath = Paths.get(FileUtils.getUserDirectoryPath(),
				Constants.APP_NAME, "ServerTest");
		if (OsUtils.isWindows()) {
			logger.info("Set root path in Windows registry: {} ", rootPath);
			WinRegistry.setRootPath(rootPath);
		}

		Injector injector = Guice.createInjector(new ApiServerModule(), new ApiServerTestModule());
		IServer cmdServer = injector.getInstance(IServer.class);
		cmdServer.start();

		logger.info("Server started...");

		// do not exit and wait
		Thread.currentThread().join();
	}
}

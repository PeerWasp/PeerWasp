package org.peerbox.guice;

import org.peerbox.server.IServer;
import org.peerbox.server.ServerFactory;
import org.peerbox.server.servlets.FileDeleteServlet;
import org.peerbox.server.servlets.FileRecoveryServlet;
import org.peerbox.server.servlets.ShareFolderServlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;

public class ApiServerModule extends ServletModule {

	@Override
	protected void configureServlets() {
		bind(FileDeleteServlet.class);
        bind(FileRecoveryServlet.class);
        bind(ShareFolderServlet.class);

        serve(ServerFactory.getContextMenuDeletePath()).with(FileDeleteServlet.class);
        serve(ServerFactory.getContextMenuVersionsPath()).with(FileRecoveryServlet.class);
        serve(ServerFactory.getContextMenuSharePath()).with(ShareFolderServlet.class);
	}

	@Provides
	IServer providesAPIServer() {
		return ServerFactory.createServer();
	}

}

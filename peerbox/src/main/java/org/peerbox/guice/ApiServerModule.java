package org.peerbox.guice;

import org.peerbox.server.IServer;
import org.peerbox.server.ServerFactory;
import org.peerbox.server.servlets.DeleteServlet;
import org.peerbox.server.servlets.ShareFolderServlet;
import org.peerbox.server.servlets.VersionsServlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;

public class ApiServerModule extends ServletModule {
	
	@Override
	protected void configureServlets() {
		bind(DeleteServlet.class);
        bind(VersionsServlet.class);
        bind(ShareFolderServlet.class);

        serve(ServerFactory.getContextMenuDeletePath()).with(DeleteServlet.class);
        serve(ServerFactory.getContextMenuVersionsPath()).with(VersionsServlet.class);
        serve(ServerFactory.getContextMenuSharePath()).with(ShareFolderServlet.class);
	}
	
	@Provides
	IServer providesAPIServer() {
		return ServerFactory.createServer();
	}

}

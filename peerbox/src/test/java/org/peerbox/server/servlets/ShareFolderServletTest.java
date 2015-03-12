package org.peerbox.server.servlets;

import org.peerbox.server.ServerFactory;

public class ShareFolderServletTest extends ServletTestSinglePath {

	public ShareFolderServletTest() {
		url = getUrl(ServerFactory.getContextMenuSharePath());
	}

}
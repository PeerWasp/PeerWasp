package org.peerbox.server.servlets;

import org.peerbox.server.ServerFactory;


public class FileRecoveryServletTest extends ServletTestSinglePath {

	public FileRecoveryServletTest() {
		url = getUrl(ServerFactory.getContextMenuVersionsPath());
	}

}

package org.peerbox.server.servlets;

import org.peerbox.server.ServerFactory;

public class FileDeleteServletTest extends ServletTestMultiplePaths {

	public FileDeleteServletTest() {
		url = getUrl(ServerFactory.getContextMenuDeletePath());
	}

}

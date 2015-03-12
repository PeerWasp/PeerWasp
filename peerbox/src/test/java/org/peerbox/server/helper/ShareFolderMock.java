package org.peerbox.server.helper;

import java.nio.file.Path;

import org.peerbox.share.IShareFolderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareFolderMock implements IShareFolderHandler {

	private static final Logger logger = LoggerFactory.getLogger(ShareFolderMock.class);

	@Override
	public void shareFolder(Path folderToShare) {
		logger.info("Share folder: {}", folderToShare);
	}

}

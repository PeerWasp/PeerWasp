package org.peerbox.server.helper;

import java.nio.file.Path;

import org.peerbox.delete.IFileDeleteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDeleteMock implements IFileDeleteHandler {

	private static final Logger logger = LoggerFactory.getLogger(FileDeleteMock.class);

	@Override
	public void deleteFile(Path fileToDelete) {
		logger.info("Delete File: '{}'", fileToDelete.toString());
	}

}

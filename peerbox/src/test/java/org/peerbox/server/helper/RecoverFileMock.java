package org.peerbox.server.helper;

import java.nio.file.Path;

import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecoverFileMock implements IFileRecoveryHandler {

	private static final Logger logger = LoggerFactory.getLogger(RecoverFileMock.class);

	@Override
	public void recoverFile(Path fileToRecover) {
		logger.info("RecoverFile: '{}'", fileToRecover.toString());
	}

}

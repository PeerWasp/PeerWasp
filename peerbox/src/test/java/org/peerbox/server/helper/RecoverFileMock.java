package org.peerbox.server.helper;

import java.nio.file.Path;

import org.peerbox.filerecovery.IFileRecoveryHandler;

public class RecoverFileMock implements IFileRecoveryHandler {

	@Override
	public void recoverFile(Path fileToRecover) {
		System.out.println(fileToRecover.toString());
	}

}

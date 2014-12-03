package org.peerbox.filerecovery;

import java.nio.file.Path;

public interface IFileRecoveryHandler {
	
	void recoverFile(Path fileToRecover);
	
}

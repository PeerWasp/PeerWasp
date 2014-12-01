package org.peerbox.interfaces;

import java.nio.file.Path;

public interface IFileVersionHandler {
	
	void onFileVersionRequested(Path fileToRecover);
	
}

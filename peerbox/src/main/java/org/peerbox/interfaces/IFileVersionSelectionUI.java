package org.peerbox.interfaces;

import java.nio.file.Path;

public interface IFileVersionSelectionUI {
	
	void show();
	
	// TODO(AA): integrate path in show call!
	void setFileToRecover(Path fileToRecover);
	
}

package org.peerbox.forcesync;

import java.nio.file.Path;

public interface IForceSyncHandler {

	void forceSync(Path topLevel);
	
	void forceSync();
}

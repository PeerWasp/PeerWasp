package org.peerbox.selectivesync;

import java.nio.file.Path;
import java.util.Map;

public interface ISynchronize {

	public int synchronize(Path fileToSync);
	public int desynchronize(Path fileToDesync);
	
	public Map<Path, Boolean> getSynchronization();
}

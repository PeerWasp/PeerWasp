package org.peerbox.watchservice;

import java.nio.file.Path;

public interface ILocalFileEventListener {
	
	public void onLocalFileCreated(Path path, boolean useFileWalker);
	public void onLocalFileDeleted(Path path);
	public void onLocalFileModified(Path path);
	
}

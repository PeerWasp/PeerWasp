package org.peerbox.watchservice;

import java.io.File;
import java.nio.file.Path;

public interface IFileEventListener {
	
	public void onFileCreated(Path path, boolean useFileWalker);
	public void onFileDeleted(Path path);
	public void onFileModified(Path path);
	
}

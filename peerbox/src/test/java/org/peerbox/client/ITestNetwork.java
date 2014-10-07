package org.peerbox.client;

import java.nio.file.Path;
import java.util.List;

public interface ITestNetwork {
	Path getBasePath();
	List<Path> getRootPaths();
	
	void start();
	void stop();
}

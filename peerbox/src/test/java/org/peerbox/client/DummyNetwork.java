package org.peerbox.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class DummyNetwork implements ITestNetwork {

	private static final int NETWORK_SIZE = 2;
	
	private Path basePath;
	private List<Path> rootPaths;
	
	public DummyNetwork() {
		basePath = Paths.get(FileUtils.getUserDirectory().toString(), "PeerBox_Test");
		rootPaths = new ArrayList<>();
	}
	
	private void initializePaths() throws IOException {
		if(!Files.exists(basePath)) {
			Files.createDirectory(basePath);
		}
		
		while(rootPaths.size() < NETWORK_SIZE) {
			Path p = Paths.get(basePath.toString(), String.format("client-%s", rootPaths.size()));
			if(!Files.exists(p)) {
				Files.createDirectory(p);
			}
			rootPaths.add(p);
		}
	}

	@Override
	public Path getBasePath() {
		return basePath;
	}

	@Override
	public List<Path> getRootPaths() {
		return rootPaths;
	}

	@Override
	public void start() {
		try {
			initializePaths();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		
	}

}

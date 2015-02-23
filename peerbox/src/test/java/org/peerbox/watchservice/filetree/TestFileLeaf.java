package org.peerbox.watchservice.filetree;

import java.nio.file.Path;

import org.peerbox.watchservice.filetree.composite.FileLeaf;

public class TestFileLeaf extends FileLeaf{

	public TestFileLeaf(Path path, boolean updateContentHash) {
		super(path, updateContentHash);
		setIsSynchronized(true);
		// TODO Auto-generated constructor stub
	}

}

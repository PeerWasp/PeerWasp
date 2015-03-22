package org.peerbox.watchservice.filetree;

import java.nio.file.Path;

import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

public class TestFolderComposite extends FolderComposite{

	public TestFolderComposite(Path path, boolean updateContentHash) {
		super(path, updateContentHash);
		setIsSynchronized(true);
	}
	
	public TestFolderComposite(Path path, boolean updateContentHash, boolean isRoot){
		super(path, updateContentHash, isRoot);
		setIsSynchronized(true);
	}
	
	/**
	 * Appends a new component to the FolderComposite. Inexistent folders are added on the
	 * fly. Existing items are replaced. Triggers updates of content and name hashes.
	 */
	@Override
	public synchronized void putComponent(Path remainingPath, FileComponent component) {
		remainingPath = stripOffPrefix(remainingPath, getPath());

		Path nextLevelPath = remainingPath.getName(0);

		// if we are at the last recursion, perform the add, else recursively continue
		if (remainingPath.getNameCount() == 1) {
			addComponentToChildren(nextLevelPath, component);
		} else {
			FileComponent nextLevel = getChildren().get(nextLevelPath);
			if (nextLevel == null) {
				// next level does not exist yet, create it
				Path childPath = constructFullPath(nextLevelPath);
				nextLevel = new TestFolderComposite(childPath, updateContentHash);
				addComponentToChildren(nextLevelPath, nextLevel);
			}
			Path newRemainingPath = remainingPath.subpath(1, remainingPath.getNameCount());
			((FolderComposite)nextLevel).putComponent(newRemainingPath, component);
		}
	}


}

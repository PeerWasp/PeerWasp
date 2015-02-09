package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;

import org.peerbox.watchservice.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLeaf extends AbstractFileComponent {

	private static final Logger logger = LoggerFactory.getLogger(FileLeaf.class);

	public FileLeaf(final Path path, boolean updateContentHash) {
		super(path, updateContentHash);

		if (updateContentHash) {
			computeContentHash();
		}
	}

	@Override
	protected boolean computeContentHash() {
		String newHash = PathUtils.computeFileContentHash(getPath());
		if (!getContentHash().equals(newHash)) {
			setContentHash(newHash);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public boolean isReady() {
		boolean parentUploaded = getParent().isUploaded();
		return parentUploaded;
	}

}
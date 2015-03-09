package org.peerbox.watchservice.filetree.persistency;

import java.util.List;

import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Stores the local FileTree in the database.
 */
class PersistLocalTree implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(PersistLocalTree.class);

	private final FileDao fileDao;
	private final FileTree fileTree;

	@Inject
	public PersistLocalTree(FileTree fileTree, FileDao fileDao) {
		this.fileTree = fileTree;
		this.fileDao = fileDao;

		// make sure table exists
		this.fileDao.createTable();
	}

	@Override
	public void run() {
		try {
			logger.info("Persist local file tree...");
			if (fileTree != null) {
				List<FileComponent> files = fileTree.asList();
				fileDao.persistAndReplaceFileComponents(files);
			}
		} catch (Exception e) {
			logger.warn("Exception while persisting local file tree.", e);
		}
	}
}

package org.peerbox.watchservice.filetree.persistency;

import java.util.List;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.app.manager.file.IFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Fetches the most recent user profile from the network and persists the
 * metadata in the database.
 */
class PersistRemoteProfile implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(PersistRemoteProfile.class);

	private final IFileManager fileManager;
	private final RemoteFileDao remoteFileDao;

	@Inject
	public PersistRemoteProfile(IFileManager fileManager, RemoteFileDao remoteFileDao) {
		this.fileManager = fileManager;
		this.remoteFileDao = remoteFileDao;

		// make sure table exists
		this.remoteFileDao.createTable();
	}

	@Override
	public void run() {
		try {
			logger.info("Persist remote profile...");
			FileNode root = fileManager.listFiles().execute();
			if (root != null) {
				List<FileNode> files = FileNode.getNodeList(root, true, true);
				remoteFileDao.persistAndReplaceFileNodes(files);
			}
		} catch (Exception e) {
			logger.warn("Exception while persisting remote profile.", e);
		}
	}
}

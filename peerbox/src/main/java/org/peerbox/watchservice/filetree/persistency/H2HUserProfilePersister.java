package org.peerbox.watchservice.filetree.persistency;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class H2HUserProfilePersister {


	private static final Logger logger = LoggerFactory.getLogger(H2HUserProfilePersister.class);

	private ScheduledExecutorService scheduler;
	private static final long SCHEDULED_DELAY = 30L;

	private final RemoteFileDao fileDao;
	private FileDao localFileDao;
	private final IFileManager fileManager;

	private FileTree localFileTree;


	@Inject
	public H2HUserProfilePersister(final IFileManager fileManager, final RemoteFileDao fileDao, final FileDao localFileDao, FileTree tree) {
		this.fileManager = fileManager;
		this.fileDao = fileDao;
		this.localFileDao = localFileDao;
		this.localFileTree = tree;
	}

	public void start() {
		fileDao.createTable();
		localFileDao.createTable();

		scheduler = Executors.newScheduledThreadPool(1);
		Runnable task = new PersistRemoteProfile();
		scheduler.scheduleWithFixedDelay(task, 0L, SCHEDULED_DELAY, TimeUnit.SECONDS);
	}

	public void stop() {
		if (scheduler != null) {
			scheduler.shutdown();
			scheduler = null;
		}
	}

	private class PersistRemoteProfile implements Runnable {
		@Override
		public void run() {
			try {

				if(localFileTree != null) {
					List<FileComponent> files = localFileTree.asList();
					localFileDao.persistAndReplaceFileComponents(files);
				}

				FileNode root = fileManager.listFiles().execute();
				if (root != null) {
					List<FileNode> files = FileNode.getNodeList(root, true, true);
					fileDao.persistAndReplaceFileNodes(files);
				}

			} catch (Exception e) {
				logger.warn("Exception while persisting remote profile.", e);
			}
		}
	}

}

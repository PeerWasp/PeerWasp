package org.peerbox.forcesync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.ClientContext;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.filetree.FileTreeInitializer;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.persistency.LocalFileDao;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForceSync {

	private static final Logger logger = LoggerFactory.getLogger(ForceSync.class);

	private ClientContext context;
	private FileEventManager fileEventManager;
	private IFileManager fileManager;

	private LocalFileDao localFileDao;
	private RemoteFileDao remoteFileDao;

	private Path topLevel;

	public ForceSync(ClientContext currentClientContext) {
		this.context = currentClientContext;

		fileEventManager = context.getFileEventManager();
		fileManager = context.getFileManager();

		localFileDao = context.getLocalFileDao();
		remoteFileDao = context.getRemoteFileDao();
	}

	public void forceSync(Path topLevel) {
		this.topLevel = topLevel;



		try {
			logger.trace("Start forced synchronization on {}", topLevel);

			FileTreeInitializer fileTreeInitializer = new FileTreeInitializer(context);
			fileTreeInitializer.initialize(topLevel);

			Set<Path> pendingEvents = fileEventManager.getPendingEvents();
			if(pendingEvents.size() > 0){
				logger.trace("New events happened during force sync. Redo.");
				pendingEvents.clear();
				forceSync(topLevel);
//				Path topLevel = pendingEvents.
//				for(Path path : pendingEvents){
//					if(path.startsWith(other))
//				}
			} else {
				fileEventManager.setCleanupRunning(false);

				try {
					synchronize(topLevel);
				} catch(Exception e) {
					logger.warn("Could not complete forced sync due to exception.", e);
				}

				context.getActionExecutor().setForceSyncRunning(false);
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void synchronize(Path topLevel) throws Exception {
		// local view
		Map<Path, FileInfo> localDisk = createLocalViewDisk();
		Map<Path, FileInfo> localDb = createLocalViewDb();

		// remote view
		Map<Path, FileInfo> remoteNetwork = createRemoteViewNetwork();
		Map<Path, FileInfo> remoteDb = createRemoteViewDb();

		ListSync listSync = new ListSync(fileEventManager, topLevel);
		listSync.sync(localDisk, localDb, remoteNetwork, remoteDb);
	}

	private Map<Path, FileInfo> createLocalViewDisk() throws IOException {
		Map<Path, FileInfo> local = new HashMap<>();
		Files.walkFileTree(topLevel, new LocalFileWalker(local));
		return local;
	}

	private Map<Path, FileInfo> createLocalViewDb() {
		Map<Path, FileInfo> localDb = new HashMap<>();
		List<FileComponent> fileList = localFileDao.getAllFiles();
		for (FileComponent c : fileList) {
			FileInfo a = new FileInfo(c);
			localDb.put(a.getPath(), a);
		}
		return localDb;
	}

	private Map<Path, FileInfo> createRemoteViewNetwork()
			throws InvalidProcessStateException, ProcessExecutionException, NoSessionException, NoPeerConnectionException {
		Map<Path, FileInfo> remoteNow = new HashMap<>();
		FileNode root = fileManager.listFiles().execute();
		List<FileNode> nodes = FileNode.getNodeList(root, true, true);
		for (FileNode node : nodes) {
			FileInfo a = new FileInfo(node);
			remoteNow.put(a.getPath(), a);
		}
		return remoteNow;
	}

	private Map<Path, FileInfo> createRemoteViewDb() {
		Map<Path, FileInfo> remoteDb = new HashMap<>();
		List<FileInfo> fileList = remoteFileDao.getAllFileNodeAttributes();
		for (FileInfo a : fileList) {
			remoteDb.put(a.getPath(), a);
		}
		return remoteDb;
	}

}

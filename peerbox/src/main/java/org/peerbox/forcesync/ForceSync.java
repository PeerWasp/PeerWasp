package org.peerbox.forcesync;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.ClientContext;
import org.peerbox.watchservice.PathUtils;
import org.peerbox.watchservice.filetree.FileTreeInitializer;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao.FileNodeAttr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForceSync {

	private static final Logger logger = LoggerFactory.getLogger(ForceSync.class);
	private ClientContext context;

	public ForceSync(ClientContext currentClientContext) {
		this.context = currentClientContext;
	}

	public void startForceSync(Path topLevel) {
		try {
			logger.trace("Start forced synchronization on {}", topLevel);
			FileTreeInitializer fileTreeInitializer = new FileTreeInitializer(context);
			fileTreeInitializer.initialize(topLevel);
			Set<Path> pendingEvents = context.getFileEventManager().getPendingEvents();
			if(pendingEvents.size() > 0){
				logger.trace("New events happened during force sync. Redo.");
				pendingEvents.clear();
				startForceSync(topLevel);
//				Path topLevel = pendingEvents.
//				for(Path path : pendingEvents){
//					if(path.startsWith(other))
//				}
			} else {
				context.getFileEventManager().setCleanupRunning(false);
				ListSync listSync = context.getInjector().getInstance(ListSync.class);
				listSync.sync();
				context.getActionExecutor().setForceSyncRunning(false);
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private Map<Path, FileInfo> createLocalViewDisk() {
		Map<Path, FileInfo> local = new HashMap<>();
		Files.walkFileTree(tree.getRootPath(), new LocalFileWalker(local));
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

	private Map<Path, FileInfo> createRemoteViewDHT() {
		Map<Path, FileInfo> remoteNow = new HashMap<>();
		FileNode root = context.getFileManager().listFiles().execute();
		List<FileNode> nodes = FileNode.getNodeList(root, true, true);
		for (FileNode node : nodes) {
			FileInfo a = new FileInfo(node);
			remoteNow.put(a.getPath(), a);
		}
		return remoteNow;
	}

	private Map<Path, FileInfo> createRemoteViewDb() {
		Map<Path, FileInfo> remoteDb = new HashMap<>();
		List<FileNodeAttr> nodeAttrs = remoteFileDao.getAllFileNodeAttributes();
		for (FileNodeAttr attr : nodeAttrs) {
			FileInfo a = new FileInfo(attr.getPath(), !attr.isFile());
			a.setHash(attr.getContentHash());
			remoteDb.put(a.getPath(), a);
		}
		return remoteDb;
	}

}

package org.peerbox.forcesync;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.ClientContext;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.PathUtils;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.persistency.FileDao;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao.FileNodeAttr;

import com.google.inject.Inject;


public class ListSync {

	private final Map<Path, FileInfo> localDb;
	private final Map<Path, FileInfo> localNow;

	private final Map<Path, FileInfo> remoteDb;
	private final Map<Path, FileInfo> remoteNow;

	private final Set<Path> foldersToDelete;
	private final Set<Path> newLocalFiles;

	private ClientContext context;
	private FileTree tree;

	private FileDao localFileDao;
	private RemoteFileDao remoteFileDao;

	@Inject
	public ListSync(ClientContext context, FileDao fileDao, RemoteFileDao remoteFileDao) {
		localDb = new HashMap<Path, FileInfo>();
		localNow = new HashMap<Path, FileInfo>();

		remoteDb = new HashMap<Path, FileInfo>();
		remoteNow = new HashMap<Path, FileInfo>();

		foldersToDelete = new HashSet<>();
		newLocalFiles = new HashSet<>();

		this.context = context;
		this.tree = context.getFileTree();

		this.localFileDao = fileDao;
		this.remoteFileDao = remoteFileDao;
		localFileDao.createTable();
		remoteFileDao.createTable();
	}

	public void sync() throws Exception {
		createLocalView();
		createRemoteView();

		synchronize();

		// delete folders if they do not have any descendants that are added
		deleteFoldersToDelete();
	}

	private void synchronize() throws Exception {
		// union of all possible files that we have to consider
		SortedSet<Path> allFiles = allFiles();

		for (Path file : allFiles) {
			/*
			 * Each file can be either [unknown, added, deleted, exists] locally or remotely
			 * This leads to 16 possible combinations
			 */
			// exists in ...?
			boolean eRemoteNow = remoteNow.containsKey(file);
			boolean eRemoteDb = remoteDb.containsKey(file);
			boolean eLocalDb = localDb.containsKey(file);
			boolean eLocalNow = localNow.containsKey(file);

			// NOTE: may be null! Only use those where the flag is true.
			// local
			FileInfo fileDisk = localNow.get(file);
			FileInfo fileLDb = localDb.get(file);
			// remote
			FileInfo fileDHT = remoteNow.get(file);
			FileInfo fileRDb = remoteDb.get(file);

			if (eRemoteNow && eRemoteDb && eLocalDb && eLocalNow) {
				/* remote: exists - local: exists */
				// file present on disk and in DHT - check hashes
				// - if dht/disk match: ok
				// - if dht/remote db match, but disk/local db mismatch: upload new version
				// - if disk/local db match, but dht/remote db mismatch: download new version
				// - otherwise: conflict

				if (fileDHT.isFile()) {
					if (hashesMatch(fileDHT, fileDisk)) {
						// sync
					} else if (hashesMatch(fileDHT, fileRDb) && !hashesMatch(fileDisk, fileLDb)) {
						uploadFile(file);
					} else if (!hashesMatch(fileDHT, fileRDb) && hashesMatch(fileDisk, fileLDb)) {
						downloadFile(file, fileDHT.isFile());
					} else {
						conflict(file);
					}
				} else {
					// folder - no update required
				}

			} else if (eRemoteNow && eRemoteDb && eLocalDb && !eLocalNow) {
				/* remote: exists - local: deleted */
				// there was a local delete
				// -> disable sync

				deleteRemoteFile(file);

			} else if (eRemoteNow && eRemoteDb && !eLocalDb && eLocalNow) {
				/* remote: exists - local: added */
				// local add, but file already present in DHT - check hashes:
				// - If dht/disk match: ok.
				// - Otherwise: conflict

				if (fileDHT.isFile()) {
					if (hashesMatch(fileDHT, fileDisk)) {
						// match - already sync
					} else {
						conflict(file);
					}
				} else {
					// folder - no content to update
				}

			} else if (eRemoteNow && eRemoteDb && !eLocalDb && !eLocalNow) {
				/* remote: exists - local: unknown */
				// file was never downloaded. Note: this has nothing to do with selective sync.
				// with selective sync, there would be an entry in the database.
				// -> download file

				downloadFile(file, fileDHT.isFile());

			} else if (eRemoteNow && !eRemoteDb && eLocalDb && eLocalNow) {
				/* remote: added - local: exists */
				// remote add, but file already exists on disk (was not uploaded yet or DB not up to date)
				// check hashes:
				// - If disk/dht match: ok
				// - If dht/local DB match: file updated locally, upload new version
				// - Otherwise: conflict

				if (fileDHT.isFile()) {
					if (hashesMatch(fileDHT, fileDisk)) {
						// match - already sync
					} else if (hashesMatch(fileDHT, fileLDb)) {
						// file was updated
						uploadFile(file);
					} else {
						conflict(file);
					}
				} else {
					// folder - no content to update
				}

			} else if (eRemoteNow && !eRemoteDb && eLocalDb && !eLocalNow) {
				/* remote: added - local: deleted */
				// local file does not exist anymore, but new remote file
				// -> download file

				downloadFile(file, fileDHT.isFile());

			} else if (eRemoteNow && !eRemoteDb && !eLocalDb && eLocalNow) {
				/* remote: added - local: added */
				// new remote file and new local file - check hashes
				// - If dht/disk match: ok
				// - Otherwise: conflict

				if (fileDHT.isFile()) {
					if (hashesMatch(fileDHT, fileDisk)) {
						// match - already sync.
					} else {
						conflict(file);
					}
				} else {
					// folder - no content to update
				}

			} else if (eRemoteNow && !eRemoteDb && !eLocalDb && !eLocalNow) {
				/* remote: added - local: unknown */
				// new remote file
				// -> download

				downloadFile(file, fileDHT.isFile());

			} else if (!eRemoteNow && eRemoteDb && eLocalDb && eLocalNow) {
				/* remote: deleted - local: exists */
				// remote file deleted
				// -> delete local IFF no local update. Otherwise, add file again.

				if (fileDisk.isFile()) {
					if (hashesMatch(fileDisk, fileLDb)) {
						deleteLocalFile(file);
					} else {
						uploadFile(file);
					}
				} else {
					deleteLocalFile(file);
					// folders are deleted at the end separately because we want to prevent accidental
					// deletion of new files in the folders that we may not have
					// processed yet.
					foldersToDelete.add(file);
				}

			} else if (!eRemoteNow && eRemoteDb && eLocalDb && !eLocalNow) {
				/* remote: deleted - local: deleted */
				// remote delete and local delete
				// -> remove entries from both databases

				removeFromRemoteDb(file);
				removeFromLocalDb(file);

			} else if (!eRemoteNow && eRemoteDb && !eLocalDb && eLocalNow) {
				/* remote: deleted - local: added */
				// remote delete and local add
				// -> add file (upload)

				uploadFile(file);

			} else if (!eRemoteNow && eRemoteDb && !eLocalDb && !eLocalNow) {
				/* remote: deleted - local: unknown */
				// file does not exist on disk nor in DHT
				// -> delete entry from remote DB

				removeFromRemoteDb(file);

			} else if (!eRemoteNow && !eRemoteDb && eLocalDb && eLocalNow) {
				/* remote: unknown - local: exists */
				// file exists, but not known to DHT
				// -> add file (upload)

				uploadFile(file);

			} else if (!eRemoteNow && !eRemoteDb && eLocalDb && !eLocalNow) {
				/* remote: unknown - local: deleted */
				// local delete, but not known to DHT
				// -> remove entry from local DB

				removeFromLocalDb(file);

			} else if (!eRemoteNow && !eRemoteDb && !eLocalDb && eLocalNow) {
				/* remote: unknown - local: added */
				// new local file, previously not known
				// -> add file (upload)

				uploadFile(file);

			} else if (!eRemoteNow && !eRemoteDb && !eLocalDb && !eLocalNow) {
				/* remote / local: unknown */
				// not possible - file does not exist
				// -> nothing to do
				// but it would mean that we have an unexpected file in the set...
				throw new Exception("Unknown combination to handle.");
			} else {
				// should never happen, all cases should be covered
				throw new Exception("Unknown combination to handle.");
			}
		}
	}

	private SortedSet<Path> allFiles() {
		SortedSet<Path> allFiles = new TreeSet<Path>();
		allFiles.addAll(localDb.keySet());
		allFiles.addAll(localNow.keySet());
		allFiles.addAll(remoteDb.keySet());
		allFiles.addAll(remoteNow.keySet());
		return allFiles;
	}

	/**
	 * Iterates through foldersToDelete and deletes the (local) folders iff there is no
	 * descendant found in newLocalFiles.
	 */
	private void deleteFoldersToDelete() {
		for (Path folder : foldersToDelete) {
			boolean hasNewDescendants = false;
			for (Path file : newLocalFiles) {
				if (file.startsWith(folder)) {
					hasNewDescendants = true;
					break;
				}
			}
			if (!hasNewDescendants) {
				deleteLocalFile(folder);
			}
		}
	}

	private void deleteLocalFile(Path file) {
		FileUtils.deleteQuietly(file.toFile());
	}

	private void deleteRemoteFile(Path file) {
		// disable sync
		FileComponent component = tree.getFile(file);
		if (component != null) {
			component.setIsSynchronized(false);
		}
	}

	private void uploadFile(Path file) {
		newLocalFiles.add(file); // used to prevent accidental removal of files
		FileEventManager eventManager = context.getFileEventManager();
		// add or update file
		if (remoteNow.containsKey(file)) {
			eventManager.onLocalFileModified(file);
		} else {
			eventManager.onLocalFileCreated(file);
		}
	}

	private void downloadFile(Path file, boolean isFile) {
		FileEventManager eventManager = context.getFileEventManager();
		eventManager.onFileUpdate(new IFileUpdateEvent() {
			@Override public File getFile() { return file.toFile(); }
			@Override public boolean isFile() { return isFile; }
			@Override public boolean isFolder() { return !isFile(); }
		});
	}

	private void conflict(Path file) {
		ConflictHandler.rename(file);
	}

	private void removeFromLocalDb(Path file) {
		localFileDao.deleteByPath(file);
	}

	private void removeFromRemoteDb(Path file) {
		remoteFileDao.deleteByPath(file);
	}

	/**
	 * Tests hashes for equality. Do not use it for folders where the hash is null!
	 *
	 * @param a an instance
	 * @param b another instance
	 * @return true if hashes are equals
	 */
	private boolean hashesMatch(FileInfo a, FileInfo b) {
		boolean match = a.getHash().equals(b.getHash());
		return match;
	}

	private String hashFile(Path path) throws IOException {
		byte[] hash = HashUtil.hash(path.toFile());
		String hash64 = PathUtils.base64Encode(hash);
		return hash64;
	}

	private void createLocalView() throws IOException {
		// disk
		Files.walkFileTree(tree.getRootPath(), new TreeBuilder());

		// DB
		List<FileComponent> local = localFileDao.getAllFiles();
		for (FileComponent c : local) {
			FileInfo a = new FileInfo(c);
			localDb.put(a.getPath(), a);
		}
	}

	private void createRemoteView() {
		try {

			// DHT
			FileNode root = context.getFileManager().listFiles().execute();
			List<FileNode> nodes = FileNode.getNodeList(root, true, true);
			for (FileNode node : nodes) {
				String hash = null;
				if(node.isFile()) {
					hash = PathUtils.base64Encode(node.getMd5());
				}
				FileInfo a = new FileInfo(node.getFile().toPath(), node.isFolder());
				a.setHash(hash);
				remoteNow.put(a.getPath(), a);
			}

			// DB
			List<FileNodeAttr> nodeAttrs = remoteFileDao.getAllFileNodeAttributes();
			for (FileNodeAttr attr : nodeAttrs) {
				FileInfo a = new FileInfo(attr.getPath(), !attr.isFile());
				a.setHash(attr.getContentHash());
				remoteDb.put(a.getPath(), a);
			}

		} catch (InvalidProcessStateException | ProcessExecutionException | NoSessionException
				| NoPeerConnectionException e) {
			e.printStackTrace();
		}
	}

	private class TreeBuilder extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			FileInfo a = new FileInfo(dir, true);
			localNow.put(a.getPath(), a);
			return super.preVisitDirectory(dir, attrs);
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			String hash = hashFile(file);
			FileInfo a = new FileInfo(file, false);
			a.setHash(hash);
			localNow.put(a.getPath(), a);
			return super.visitFile(file, attrs);
		}
	}


}

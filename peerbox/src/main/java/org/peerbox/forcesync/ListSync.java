package org.peerbox.forcesync;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.events.implementations.FileDeleteEvent;
import org.hive2hive.core.events.implementations.FileUpdateEvent;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


public class ListSync {

	private Map<Path, FileInfo> localDb;
	private Map<Path, FileInfo> localDisk;

	private Map<Path, FileInfo> remoteDb;
	private Map<Path, FileInfo> remoteNetwork;

	// folders to delete locally (due to a deleted folder in the network)
	private final Set<Path> foldersToDelete;
	// all files that are uploaded (either the file is new or updated)
	private final Set<Path> newLocalFiles;

	private final FileEventManager fileEventManager;

	@Inject
	public ListSync(FileEventManager fileEventManager) {
		this.fileEventManager = fileEventManager;

		foldersToDelete = new HashSet<>();
		newLocalFiles = new HashSet<>();
	}

	public void sync(
			Map<Path, FileInfo> localDisk,
			Map<Path, FileInfo> localDb,
			Map<Path, FileInfo> remoteNetwork,
			Map<Path, FileInfo> remoteDb) throws Exception {

		// make sure clients set these instances
		Preconditions.checkNotNull(fileEventManager);
		Preconditions.checkNotNull(localDb);
		Preconditions.checkNotNull(localDisk);
		Preconditions.checkNotNull(remoteDb);
		Preconditions.checkNotNull(remoteNetwork);

		this.localDisk = localDisk;
		this.localDb = localDb;
		this.remoteNetwork = remoteNetwork;
		this.remoteDb = remoteDb;

		foldersToDelete.clear();
		newLocalFiles.clear();

		// perform a list sync using the maps
		synchronize();

		// delete folders if they do not have any descendants that are added
		deleteFoldersToDelete();

		cleanup();
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
			boolean eRemoteNetwork = remoteNetwork.containsKey(file);
			boolean eRemoteDb = remoteDb.containsKey(file);
			boolean eLocalDb = localDb.containsKey(file);
			boolean eLocalDisk = localDisk.containsKey(file);

			// NOTE: may be null! Only use those where the flag is true.
			// local
			FileInfo fileDisk = localDisk.get(file);
			FileInfo fileLocalDb = localDb.get(file);
			// remote
			FileInfo fileNetwork = remoteNetwork.get(file);
			FileInfo fileRemoteDb = remoteDb.get(file);

			if (eRemoteNetwork && eRemoteDb && eLocalDb && eLocalDisk) {
				/* remote: exists - local: exists */
				// file present on disk and in network - check hashes
				// - if network/disk match: ok
				// - if network/remote db match, but disk/local db mismatch: upload new version
				// - if disk/local db match, but network/remote db mismatch: download new version
				// - otherwise: conflict

				if (fileNetwork.isFile()) {
					if (hashesMatch(fileNetwork, fileDisk)) {
						// sync
					} else if (hashesMatch(fileNetwork, fileRemoteDb) && !hashesMatch(fileDisk, fileLocalDb)) {
						uploadFile(file);
					} else if (!hashesMatch(fileNetwork, fileRemoteDb) && hashesMatch(fileDisk, fileLocalDb)) {
						downloadFile(file, fileNetwork.isFile());
					} else {
						conflict(file);
					}
				} else {
					// folder - no update required
				}

			} else if (eRemoteNetwork && eRemoteDb && eLocalDb && !eLocalDisk) {
				/* remote: exists - local: deleted */
				// there was a local delete
				// -> disable sync

				deleteRemoteFile(file);

			} else if (eRemoteNetwork && eRemoteDb && !eLocalDb && eLocalDisk) {
				/* remote: exists - local: added */
				// local add, but file already present in network - check hashes:
				// - If network/disk match: ok.
				// - Otherwise: conflict

				if (fileNetwork.isFile()) {
					if (hashesMatch(fileNetwork, fileDisk)) {
						// match - already sync
					} else {
						conflict(file);
					}
				} else {
					// folder - no content to update
				}

			} else if (eRemoteNetwork && eRemoteDb && !eLocalDb && !eLocalDisk) {
				/* remote: exists - local: unknown */
				// file was never downloaded. Note: this has nothing to do with selective sync.
				// with selective sync, there would be an entry in the database.
				// -> download file

				downloadFile(file, fileNetwork.isFile());

			} else if (eRemoteNetwork && !eRemoteDb && eLocalDb && eLocalDisk) {
				/* remote: added - local: exists */
				// remote add, but file already exists on disk (was not uploaded yet or DB not up to date)
				// check hashes:
				// - If disk/network match: ok
				// - If network/local DB match: file updated locally, upload new version
				// - Otherwise: conflict

				if (fileNetwork.isFile()) {
					if (hashesMatch(fileNetwork, fileDisk)) {
						// match - already sync
					} else if (hashesMatch(fileNetwork, fileLocalDb)) {
						// file was updated
						uploadFile(file);
					} else {
						conflict(file);
					}
				} else {
					// folder - no content to update
				}

			} else if (eRemoteNetwork && !eRemoteDb && eLocalDb && !eLocalDisk) {
				/* remote: added - local: deleted */
				// local file does not exist anymore, but new remote file
				// -> download file

				downloadFile(file, fileNetwork.isFile());

			} else if (eRemoteNetwork && !eRemoteDb && !eLocalDb && eLocalDisk) {
				/* remote: added - local: added */
				// new remote file and new local file - check hashes
				// - If network/disk match: ok
				// - Otherwise: conflict

				if (fileNetwork.isFile()) {
					if (hashesMatch(fileNetwork, fileDisk)) {
						// match - already sync.
					} else {
						conflict(file);
					}
				} else {
					// folder - no content to update
				}

			} else if (eRemoteNetwork && !eRemoteDb && !eLocalDb && !eLocalDisk) {
				/* remote: added - local: unknown */
				// new remote file
				// -> download

				downloadFile(file, fileNetwork.isFile());

			} else if (!eRemoteNetwork && eRemoteDb && eLocalDb && eLocalDisk) {
				/* remote: deleted - local: exists */
				// remote file deleted
				// -> delete local IFF no local update. Otherwise, add file again.

				if (fileDisk.isFile()) {
					if (hashesMatch(fileDisk, fileLocalDb)) {
						deleteLocalFile(file, true);
					} else {
						uploadFile(file);
					}
				} else {
					// folders are deleted at the end separately because we want to prevent accidental
					// deletion of new files in the folders that we may not have
					// processed yet.
					foldersToDelete.add(file);
				}

			} else if (!eRemoteNetwork && eRemoteDb && eLocalDb && !eLocalDisk) {
				/* remote: deleted - local: deleted */
				// remote delete and local delete
				// -> remove entries from both databases

				removeFromRemoteDb(file);
				removeFromLocalDb(file);

			} else if (!eRemoteNetwork && eRemoteDb && !eLocalDb && eLocalDisk) {
				/* remote: deleted - local: added */
				// remote delete and local add
				// -> add file (upload)

				uploadFile(file);

			} else if (!eRemoteNetwork && eRemoteDb && !eLocalDb && !eLocalDisk) {
				/* remote: deleted - local: unknown */
				// file does not exist on disk nor in network
				// -> delete entry from remote DB

				removeFromRemoteDb(file);

			} else if (!eRemoteNetwork && !eRemoteDb && eLocalDb && eLocalDisk) {
				/* remote: unknown - local: exists */
				// file exists, but not known to network
				// -> add file (upload)

				uploadFile(file);

			} else if (!eRemoteNetwork && !eRemoteDb && eLocalDb && !eLocalDisk) {
				/* remote: unknown - local: deleted */
				// local delete, but not known to network
				// -> remove entry from local DB

				removeFromLocalDb(file);

			} else if (!eRemoteNetwork && !eRemoteDb && !eLocalDb && eLocalDisk) {
				/* remote: unknown - local: added */
				// new local file, previously not known
				// -> add file (upload)

				uploadFile(file);

			} else if (!eRemoteNetwork && !eRemoteDb && !eLocalDb && !eLocalDisk) {
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
		allFiles.addAll(localDisk.keySet());
		allFiles.addAll(remoteDb.keySet());
		allFiles.addAll(remoteNetwork.keySet());
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
				deleteLocalFile(folder, false);
			}
		}
	}

	private void deleteLocalFile(Path file, boolean isFile) {
		// delete the local file due to a remote delete
		IFileDeleteEvent deleteEvent = new FileDeleteEvent(file.toFile(), isFile);
		fileEventManager.onFileDelete(deleteEvent);
	}

	private void deleteRemoteFile(Path file) {
		// soft delete due to a local delete
		// no hard delete of files: only disable sync
		fileEventManager.onFileDesynchronized(file);
	}

	private void uploadFile(Path file) {
		// used to prevent accidental removal of files
		newLocalFiles.add(file);
		// add or update file depending on whether it already exists in network
		if (remoteNetwork.containsKey(file)) {
			fileEventManager.onLocalFileModified(file);
		} else {
			fileEventManager.onLocalFileCreated(file);
		}
	}

	private void downloadFile(Path file, boolean isFile) {
		// file update event will trigger download
		IFileUpdateEvent updateEvent = new FileUpdateEvent(file.toFile(), isFile);
		fileEventManager.onFileUpdate(updateEvent);
	}

	private void conflict(Path file) {
		ConflictHandler.resolveConflict(file);
	}

	private void removeFromLocalDb(Path file) {
		// TODO(AA): either delete it here or perform persistence task after sync
		// localFileDao.deleteByPath(file);
	}

	private void removeFromRemoteDb(Path file) {
		// TODO(AA): either delete it here or perform persistence task after sync
		// remoteFileDao.deleteByPath(file);
	}

	/**
	 * Tests hashes for equality. Do not use it for folders where the hash is null!
	 *
	 * @param a an instance
	 * @param b another instance
	 * @return true if hashes are equals
	 */
	private boolean hashesMatch(FileInfo a, FileInfo b) {
		boolean match = a.getContentHash().equals(b.getContentHash());
		return match;
	}

	private void cleanup() {
		localDb = null;
		localDisk = null;
		remoteDb = null;
		remoteNetwork = null;
		foldersToDelete.clear();
		newLocalFiles.clear();
	}

}

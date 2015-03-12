package org.peerbox.forcesync;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.peerbox.app.manager.file.FileInfo;

public class RemoteDeleteLocalExists extends ListSyncTest {

	@Test
	public void testRemoteDeleteLocalExists_File_SameHashes() throws Exception {
		remoteDatabase.put(filePath, file1);

		localDatabase.put(filePath, file1);
		local.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onFileDelete(Matchers.any(IFileDeleteEvent.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void testRemoteDeleteLocalExists_File_DifferentHashes() throws Exception {
		remoteDatabase.put(filePath, file1);

		localDatabase.put(filePath, file1);
		local.put(filePath, file2);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onLocalFileCreated(Matchers.any(Path.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void testRemoteDeleteLocalExists_Folder_NoNewFile() throws Exception {
		Path folderPath = Paths.get(basePath.toString(), "folder");
		FileInfo folderInfo = new FileInfo(folderPath, true);

		Path filePath = folderPath.resolve("file.txt");
		FileInfo fileInfo = new FileInfo(filePath, false, "hash1");

		remoteDatabase.put(folderPath, folderInfo);
		remoteDatabase.put(filePath, fileInfo);

		localDatabase.put(folderPath, folderInfo);
		localDatabase.put(filePath, fileInfo);

		local.put(folderPath, folderInfo);
		local.put(filePath, fileInfo);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager, Mockito.times(2)).onFileDelete(Matchers.any(IFileDeleteEvent.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void testRemoteDeleteLocalExists_Folder_WithNewFile() throws Exception {
		// scenario: folder with two files. one file is new/updated and the other one unchanged
		// folder was deleted in network
		// expected result: new/updated file not deleted, but added. old unchanged file deleted.

		// parent folder (deleted in network)
		Path folderPath = Paths.get(basePath.toString(),"folder");
		FileInfo folderInfo = new FileInfo(folderPath, true);
		remoteDatabase.put(folderPath, folderInfo);
		local.put(folderPath, folderInfo);
		localDatabase.put(folderPath, folderInfo);

		// file: new/updated file -> hash do not match
		Path fileNewPath = folderPath.resolve("fileNew.txt");
		FileInfo fileNewInfo = new FileInfo(fileNewPath, false, "hashNew");
		local.put(fileNewPath, fileNewInfo);

		// old unchanged file -> does not exist in network anymore, but locally
		Path fileOldPath = folderPath.resolve("fileOld.txt");
		FileInfo fileOldInfo = new FileInfo(fileOldPath, false, "hashOld");
		remoteDatabase.put(fileOldPath, fileOldInfo);
		local.put(fileOldPath, fileOldInfo);
		localDatabase.put(fileOldPath, fileOldInfo);


		listSync.sync(local, localDatabase, remote, remoteDatabase);

		// old unchanged file -> deleted
		Mockito.verify(fileEventManager, Mockito.times(1)).onFileDelete(Matchers.any(IFileDeleteEvent.class));

		// new / updated file -> added
		Mockito.verify(fileEventManager, Mockito.times(1)).onLocalFileCreated(fileNewPath);

		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

}

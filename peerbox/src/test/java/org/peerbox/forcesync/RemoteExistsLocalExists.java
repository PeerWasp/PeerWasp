package org.peerbox.forcesync;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConflictHandler.class)
public class RemoteExistsLocalExists extends ListSyncTest {

	@Test
	public void localAndRemoteVersionEqual() throws Exception{
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void localFilesDifferentTest() throws Exception {
		local.put(filePath, file2);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onLocalFileModified(filePath);
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void remoteFilesDifferentTest() throws Exception {
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file2);
		remoteDatabase.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onFileUpdate(Matchers.any(IFileUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}


	@Test
	public void localAndRemoteFilesDifferentTest() throws Exception {
		PowerMockito.mockStatic(ConflictHandler.class);

		local.put(filePath, file1);
		localDatabase.put(filePath, file2);
		remote.put(filePath, file3);
		remoteDatabase.put(filePath, file4);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		PowerMockito.stub(PowerMockito.method(ConflictHandler.class, "rename")).toReturn(Paths.get("asdf"));
		PowerMockito.verifyStatic();
		ConflictHandler.resolveConflict(Matchers.any(Path.class));

		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void folderTest() throws Exception {
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
}

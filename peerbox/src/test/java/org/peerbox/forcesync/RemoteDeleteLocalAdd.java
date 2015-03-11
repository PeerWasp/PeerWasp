package org.peerbox.forcesync;

import org.junit.Test;
import org.mockito.Mockito;

public class RemoteDeleteLocalAdd extends ListSyncTest {

	@Test
	public void testRemoteDeleteLocalAdd_SameHashes() throws Exception {
		remoteDatabase.put(filePath, file1);
		local.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onLocalFileCreated(filePath);
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void testRemoteDeleteLocalAdd_DifferentHashes() throws Exception {
		remoteDatabase.put(filePath, file1);
		local.put(filePath, file2);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onLocalFileCreated(filePath);
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

}

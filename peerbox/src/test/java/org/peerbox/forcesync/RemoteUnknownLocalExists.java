package org.peerbox.forcesync;

import org.junit.Test;
import org.mockito.Mockito;

public class RemoteUnknownLocalExists extends ListSyncTest {

	@Test
	public void testLocalExists_SameHashes() throws Exception {
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onLocalFileCreated(filePath);
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void testLocalExists_DifferentHashes() throws Exception {
		local.put(filePath, file1);
		localDatabase.put(filePath, file2);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onLocalFileCreated(filePath);
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

}

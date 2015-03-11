package org.peerbox.forcesync;

import org.junit.Test;
import org.mockito.Mockito;

public class RemoteDeleteLocalUnknown extends ListSyncTest {

	@Test
	public void testRemoteDelete() throws Exception {
		remoteDatabase.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		// no interactions since it does not exist in network or locally
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

}

package org.peerbox.forcesync;

import org.junit.Test;
import org.mockito.Mockito;

public class RemoteDeleteLocalDelete extends ListSyncTest {

	@Test
	public void testRemoteDeleteLocalDelete() throws Exception {
		remoteDatabase.put(filePath, file1);
		localDatabase.put(filePath, file2);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		// NO file event manager interactions since it only exists in databases
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

}

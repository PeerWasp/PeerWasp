package org.peerbox.forcesync;

import org.junit.Test;
import org.mockito.Mockito;

public class RemoteUnknownLocalDelete extends ListSyncTest {

	@Test
	public void testLocalDelete() throws Exception {
		localDatabase.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		// no interactions because it does not exist in network and not anymore locally
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

}

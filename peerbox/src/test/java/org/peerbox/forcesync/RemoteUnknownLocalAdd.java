package org.peerbox.forcesync;

import java.nio.file.Path;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class RemoteUnknownLocalAdd extends ListSyncTest {

	@Test
	public void testLocalAdd() throws Exception {
		local.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onLocalFileCreated(Matchers.any(Path.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
}

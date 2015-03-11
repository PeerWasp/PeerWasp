package org.peerbox.forcesync;

import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class RemoteExistsLocalUnknown extends ListSyncTest {

	
	@Test
	public void onlyMissingLocallyInBoth() throws Exception {
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verify(fileEventManager).onFileUpdate(Matchers.any(IFileUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
		
	}
}

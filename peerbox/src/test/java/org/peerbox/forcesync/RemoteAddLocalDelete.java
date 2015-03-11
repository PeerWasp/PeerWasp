package org.peerbox.forcesync;

import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class RemoteAddLocalDelete extends ListSyncTest{

	@Test
	public void downloadFile() throws Exception {
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verify(fileEventManager).onFileUpdate(Matchers.any(IFileUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
}

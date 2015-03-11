package org.peerbox.forcesync;

import java.nio.file.Path;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class RemoteExistsLocalDelete extends ListSyncTest {

	
	@Test
	public void defaultTest() throws Exception{
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verify(fileEventManager).onFileDesynchronized(Matchers.any(Path.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
}

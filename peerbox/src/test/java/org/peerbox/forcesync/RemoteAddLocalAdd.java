package org.peerbox.forcesync;

import java.nio.file.Path;

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
public class RemoteAddLocalAdd extends ListSyncTest{

	@Test
	public void localAndRemoteVersionEqual() throws Exception{
		local.put(filePath, file1);
		remote.put(filePath, file1);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		Mockito.verifyNoMoreInteractions(fileEventManager);
	}

	@Test
	public void localAndRemoteVersionNotEqual() throws Exception {
		PowerMockito.mockStatic(ConflictHandler.class);

		local.put(filePath, file1);
		remote.put(filePath, file2);

		listSync.sync(local, localDatabase, remote, remoteDatabase);

		PowerMockito.stub(PowerMockito.method(ConflictHandler.class, "rename"));
		PowerMockito.verifyStatic();
		ConflictHandler.resolveConflict(Matchers.any(Path.class));

		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
}

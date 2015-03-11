package org.peerbox.forcesync;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
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
public class RemoteAddLocalExists extends ListSyncTest {

	@Test
	public void contentEqualsRemoteAndLocal() throws Exception {
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
	
	@Test
	public void contentEqualsRemoteAndLocalDatabase() throws Exception {
		local.put(filePath, file2);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verify(fileEventManager).onLocalFileModified(Matchers.any(Path.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
	
	@Test
	public void conflict() throws Exception {
		local.put(filePath, file2);
		localDatabase.put(filePath, file2);
		remote.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		PowerMockito.stub(PowerMockito.method(ConflictHandler.class, "rename")).toReturn(Paths.get("asdf"));
		PowerMockito.verifyStatic();
		ConflictHandler.rename(Matchers.any(Path.class));
		
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
	
}

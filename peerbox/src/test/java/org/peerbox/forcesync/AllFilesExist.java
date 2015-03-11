package org.peerbox.forcesync;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConflictHandler.class)
public class AllFilesExist extends ListSyncTest {

	@Mock private FileEventManager fileEventManager;
	
	@Mock private ConflictHandler conflictHandler;
	
	private Map<Path, FileInfo> local;
	private Map<Path, FileInfo> localDatabase;
	private Map<Path, FileInfo> remote;
	private Map<Path, FileInfo> remoteDatabase;
	
	private Path filePath = Paths.get("file.txt");
	private FileInfo file1 = new FileInfo(filePath, false, "hash1");
	private FileInfo file2 = new FileInfo(filePath, false, "hash2");
	private FileInfo file3 = new FileInfo(filePath, false, "hash3");
	private FileInfo file4 = new FileInfo(filePath, false, "hash4");
	
	private ListSync listSync;
	
	@Before
	public void setup(){
		local = new HashMap<Path, FileInfo>();
		localDatabase = new HashMap<Path, FileInfo>();
		remote = new HashMap<Path, FileInfo>();
		remoteDatabase = new HashMap<Path, FileInfo>();
		
		listSync = new ListSync(fileEventManager);
	}
	
	@Test
	public void localAndRemoteVersionEqual() throws Exception{
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
	
	@Test
	public void localFilesDifferentTest() throws Exception {
		local.put(filePath, file2);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verify(fileEventManager).onLocalFileModified(filePath);
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
	
	@Test
	public void remoteFilesDifferentTest() throws Exception {
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file2);
		remoteDatabase.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verify(fileEventManager).onFileUpdate(Matchers.any(IFileUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
	
	
	@Test
	public void localAndRemoteFilesDifferentTest() throws Exception {
		PowerMockito.mockStatic(ConflictHandler.class);

		local.put(filePath, file1);
		localDatabase.put(filePath, file2);
		remote.put(filePath, file3);
		remoteDatabase.put(filePath, file4);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);

		PowerMockito.stub(PowerMockito.method(ConflictHandler.class, "rename")).toReturn(Paths.get("asdf"));
		PowerMockito.verifyStatic();
		ConflictHandler.rename(Matchers.any(Path.class));
		
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
	
	@Test
	public void folderTest() throws Exception {	
		local.put(filePath, file1);
		localDatabase.put(filePath, file1);
		remote.put(filePath, file1);
		remoteDatabase.put(filePath, file1);
		
		listSync.sync(local, localDatabase, remote, remoteDatabase);
		
		Mockito.verifyNoMoreInteractions(fileEventManager);
	}
}

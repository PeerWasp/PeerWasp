package org.peerbox.forcesync;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.peerbox.BaseJUnitTest;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ListSyncTest extends BaseJUnitTest{

	@Mock protected FileEventManager fileEventManager;
	
	@Mock protected ConflictHandler conflictHandler;
	
	protected Map<Path, FileInfo> local;
	protected Map<Path, FileInfo> localDatabase;
	protected Map<Path, FileInfo> remote;
	protected Map<Path, FileInfo> remoteDatabase;
	
	protected Path filePath = Paths.get("file.txt");
	protected FileInfo file1 = new FileInfo(filePath, false, "hash1");
	protected FileInfo file2 = new FileInfo(filePath, false, "hash2");
	protected FileInfo file3 = new FileInfo(filePath, false, "hash3");
	protected FileInfo file4 = new FileInfo(filePath, false, "hash4");
	
	protected ListSync listSync;
	
	@Before
	public void setup(){
		local = new HashMap<Path, FileInfo>();
		localDatabase = new HashMap<Path, FileInfo>();
		remote = new HashMap<Path, FileInfo>();
		remoteDatabase = new HashMap<Path, FileInfo>();
		
		listSync = new ListSync(fileEventManager);
	}

}

package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.peerbox.utils.FileTestUtils;

public class ConflictTest extends FileIntegrationTest{
	
	@Test
	public void LocalCreate_RemoteCreateTest() throws IOException, InterruptedException {
		
		String homeDir = System.getProperty("user.home");
		
		Path pathClient0 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "test.txt");
		Path pathClient1 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-1" + File.separator + "test.txt");
		
		FileUtils.writeStringToFile(pathClient0.toFile(), "Client0_FIRST");
		Thread.sleep(2000);
		FileUtils.writeStringToFile(pathClient1.toFile(), "_CLIENT1_SECOND");
		Thread.sleep(10000);
		assertSyncClientPaths();
	}
	
	@Test
	public void LocalUpdate_RemoteDeleteTest() throws IOException, InterruptedException {
		
		String homeDir = System.getProperty("user.home");
		
		Path pathClient0 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "test.txt");
		Path pathClient1 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-1" + File.separator + "test.txt");
		
		FileUtils.writeStringToFile(pathClient0.toFile(), "CLIENT0_FIRST");
		Thread.sleep(5000); // wait until file is synched
		FileUtils.forceDelete(pathClient0.toFile());
		Thread.sleep(2000);
		FileUtils.writeStringToFile(pathClient1.toFile(), "_CLIENT1_SECOND");
		Thread.sleep(10000);
		assertSyncClientPaths();

	}
	
	// move detection seems not to work yet (no synchronization)
	@Test
	public void LocalMove_RemoteUpdateTest() throws IOException, InterruptedException {
		
		String homeDir = System.getProperty("user.home");
		
		Path pathClient0 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "test.txt");
		Path pathClient1 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-1" + File.separator + "test.txt");
		Path pathClient0_subDir = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "subDir");
		
		FileUtils.writeStringToFile(pathClient0.toFile(), "CLIENT0_FIRST");
		Thread.sleep(5000); // wait until file is synched
		FileUtils.moveFileToDirectory(pathClient0.toFile(), pathClient0_subDir.toFile(), true);
		Thread.sleep(3000);
		//FileUtils.writeStringToFile(pathClient1.toFile(), "ABCDEFGHIJ0123456789");
		Thread.sleep(10000);
		assertSyncClientPaths();

	}
	
	@Test
	public void LocalUpdate_RemoteUpdateTest() throws IOException, InterruptedException {
		
		String homeDir = System.getProperty("user.home");
		
		Path pathClient0 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "test.txt");
		Path pathClient1 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-1" + File.separator + "test.txt");
		
		FileUtils.writeStringToFile(pathClient0.toFile(), "CLIENT0_FIRST");
		Thread.sleep(5000);
		FileUtils.writeStringToFile(pathClient0.toFile(), "_CLIENT0_SECOND");
		Thread.sleep(2000);
		FileUtils.writeStringToFile(pathClient1.toFile(), "_CLIENT1_THIRD");
		Thread.sleep(10000);
		assertSyncClientPaths();
	}
}

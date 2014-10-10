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
	public void singleFileTest() throws IOException, InterruptedException {
		
		String homeDir = System.getProperty("user.home");
		// ADD
		Path file_1 = addSingleFile();
		
		Path pathUser1 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "test.txt");
		Path pathUser2 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-1" + File.separator + "test.txt");
		
		FileUtils.writeStringToFile(pathUser1.toFile(), "ABCDEFGHIJ");
		FileUtils.writeStringToFile(pathUser2.toFile(), "0123456789");
		
		System.out.println("FILE CONTENT USER 1: " + FileUtils.readFileToString(pathUser1.toFile()));
		System.out.println("FILE CONTENT USER 2: " + FileUtils.readFileToString(pathUser2.toFile()));
		
		Thread.sleep(10000);
		assertSyncClientPaths();
		
		System.out.println("FILE CONTENT USER 1 AFTER SYNC: " + FileUtils.readFileToString(pathUser1.toFile()));
		System.out.println("FILE CONTENT USER 2 AFTER SYNC: " + FileUtils.readFileToString(pathUser2.toFile()));

	}
	
	protected Path addSingleFile() throws IOException {
		Path file = FileTestUtils.createTestFile(masterRootPath, NUMBER_OF_CHARS);
		
		waitForExists(file, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return file;
	}
}

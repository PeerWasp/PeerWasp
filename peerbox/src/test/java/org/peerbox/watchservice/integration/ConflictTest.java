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

import org.junit.Test;
import org.peerbox.utils.FileTestUtils;

public class ConflictTest extends FileIntegrationTest{
	
	@Test
	public void singleFileTest() throws IOException {
		// ADD
		Path file_1 = addSingleFile();
		Path file_2 = addSingleFile();
	}
	
	protected Path addSingleFile() throws IOException {
		Path file = FileTestUtils.createTestFile(client.getRootPath(), NUMBER_OF_CHARS);
		
		waitForExists(file, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return file;
	}
}

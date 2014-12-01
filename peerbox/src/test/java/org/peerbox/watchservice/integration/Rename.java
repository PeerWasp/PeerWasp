package org.peerbox.watchservice.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.peerbox.utils.FileTestUtils;


public class Rename extends FileIntegrationTest{
	
	/**
	 * These tests only consider file and folder contents. The difference 
	 * between delete/create and move is not tested yet!
	 * @throws IOException
	 * @throws InterruptedException 
	 */	
	@Test
	public void singleFileRenameTest() throws IOException, InterruptedException{
		Path initialFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		waitForExists(initialFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		
		Path renamedFile = rename(initialFile, "RENAMED.file");
		initialFile = renamedFile;
		
		
		assertSyncClientPaths();
	}
	

	Path rename(Path oldName, String newNameString) throws IOException{
	    return Files.move(oldName, oldName.resolveSibling(newNameString));
	}
	

	
	
}

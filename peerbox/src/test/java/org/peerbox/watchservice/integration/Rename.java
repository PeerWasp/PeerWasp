package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;


public class Rename extends FileIntegrationTest{

	/**
	 * These tests only consider file and folder contents. The difference
	 * between delete/create and move is not tested yet!
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void singleFileRenameTest() throws IOException, InterruptedException{
		Path initialFile = addFile(true);
		assertCleanedUpState(1);

		Path renamedFile = rename(initialFile, "RENAMED.file");
		waitForExists(renamedFile, WAIT_TIME_SHORT);
		waitForNotExists(initialFile, WAIT_TIME_SHORT);
		assertCleanedUpState(1);
	}


	Path rename(Path oldName, String newNameString) throws IOException{
	    return Files.move(oldName, oldName.resolveSibling(newNameString));
	}




}

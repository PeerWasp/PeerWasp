package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author Claudio Anliker
 * 
 * This set of integration tests is designed to verify correct end-to-end execution 
 * of update operations. Every test consists of two stages. In the first stage, files 
 * and/or folders are created. In the second stage, they are modified on disk. This
 * leads to LocalUpdateEvents that are propagated to other clients. At the 
 * end of  both stages, the following checks are performed:
 *
 * - Do the two folders contain the same objects?
 * - Are any pending executions left in the queue (this indicates a bug in the FileEventManager)?
 * - Is the number of objects contained in both folders as expected (i.e. same file might be 
 * missing in both places)?
 * 
 * Besides that, the test waits for a specified maximal amount of time after the first stage if
 * files are missing. This is done to discover incorrect/incomplete synchronization.
 */
public class Update extends FileIntegrationTest {
	
	@Test 
	public void singleFileTest() throws IOException {
		Path f = addSingleFile();
		assertCleanedUpState(1);
		
		updateSingleFile(f, true);
		assertCleanedUpState(1);
	}
	
	@Test
	public void manyFilesTest() throws IOException {
		int nrFiles = 100;
		List<Path> files = addManyFiles(nrFiles, WAIT_TIME_LONG);
		assertCleanedUpState(nrFiles);
		
		updateManyFiles(files);
		assertCleanedUpState(nrFiles);
	}
	
	@Test 
	public void singleFileInFolderTest() throws IOException {
		List<Path> paths = addSingleFileInFolder();
		assertCleanedUpState(2);
		
		updateSingleFile(paths.get(1), true);
		assertCleanedUpState(2);
	}
	
	@Test
	public void manyFilesInFolderTest() throws IOException {
		List<Path> files = addManyFilesInFolder(100);
		assertCleanedUpState(101);

		updateManyFiles(files);
		assertCleanedUpState(101);
	}
	
	//TODO: this test fails only in conjunction with others: check!
	@Test
	public void manyFilesInManyFoldersTest() throws IOException {
		int nrFolders = 10;
		int nrFilesPerFolder = 10;
		int totalFiles = nrFolders + nrFolders * nrFilesPerFolder;
		List<Path> files = addManyFilesInManyFolders(10, 10);
		assertCleanedUpState(totalFiles);
		
		updateManyFiles(files);
		assertCleanedUpState(totalFiles);
	}


	private List<Path> updateManyFiles(List<Path> files) throws IOException {
		List<Path> modified = new ArrayList<>();
		for(int i = 0; i < files.size()/2; ++i) {
			Path f = files.get(i);
			// ignore directories
			if(Files.isDirectory(f)) { 
				continue;
			}
			
			// modify with probability 0.5
			boolean modify = true; //(RandomUtils.nextInt(0, 100) % 2) == 0;
			if (modify) {
				updateSingleFile(f, false);
				modified.add(f);
			}
		}
		waitForUpdate(modified, WAIT_TIME_LONG);
		return modified;
	}
}

package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.peerbox.utils.FileTestUtils;

public class Update extends FileIntegrationTest {
	
	@Test 
	public void singleFileTest() throws IOException {
		// ADD
		Path f = addSingleFile();
		logger.info("Adding finished.");
		
		// UPDATE
		updateSingleFile(f);
		
		waitForUpdate(f, WAIT_TIME_SHORT);
		assertSyncClientPaths();
	}
	
	@Test
	public void manyFilesTest() throws IOException {
		// ADD
		List<Path> files = addManyFiles();
		logger.info("Adding finished.");
		
		// UPDATE
		List<Path> modified = updateManyFiles(files);
		
		waitForUpdate(modified, WAIT_TIME_LONG);
		assertSyncClientPaths();
	}
	
	@Test 
	public void singleFileInFolderTest() throws IOException {
		// ADD
		List<Path> paths = addSingleFileInFolder();
		logger.info("Adding finished.");
		
		// UPDATE
		Path f = paths.get(1);
		updateSingleFile(f);
		
		waitForUpdate(f, WAIT_TIME_SHORT);
		assertSyncClientPaths();
	}
	
	@Test
	public void manyFilesInFolderTest() throws IOException {
		// ADD
		List<Path> files = addManyFilesInFolder();
		logger.info("Adding finished.");
		
		// UPDATE
		List<Path> modified = updateManyFiles(files);
		
		waitForUpdate(modified, WAIT_TIME_LONG);
		assertSyncClientPaths();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void manyFilesInManyFoldersTest() throws IOException {
		// ADD
		List<Path> files = addManyFilesInManyFolders();
		logger.info("Adding finished.");
		
		// UPDATE
		List<Path> modified = updateManyFiles(files);
		
		waitForUpdate(modified, WAIT_TIME_LONG);
		assertSyncClientPaths();
	}

	private void updateSingleFile(Path f) throws IOException {
		double scale = RandomUtils.nextDouble(0.01, 2.0);
//		FileTestUtils.writeRandomData(f, (int)(NUMBER_OF_CHARS*scale));
		FileTestUtils.writeRandomData(f, 100000);
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
				updateSingleFile(f);
				modified.add(f);
			}
		}
		return modified;
	}
}

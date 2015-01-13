package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.junit.Ignore;
import org.junit.Test;
import org.peerbox.testutils.FileTestUtils;

/**
 * @author Claudio Anliker
 * 
 * This set of integration tests is designed to verify correct end-to-end execution of add 
 * and delete operations. Every test consists of two stages. In the first stage, files 
 * and/or folders are created. In the second stage, they are deleted again. At the end of 
 * both stages, the following checks are performed:
 * 
 * - Do the two folders contain the same objects?
 * - Are any pending executions left in the queue (this indicates a bug in the FileEventManager)?
 * - Is the number of objects contained in both folders as expected (i.e. same file might be 
 * missing in both places)?
 * 
 * Besides that, the test waits for a specified maximal amount of time after the first stage if
 * files are missing. This is done to discover incorrect/incomplete synchronization.
 */
public class AddDelete extends FileIntegrationTest {
	
	/**
	 * This test verifies the correct add and delete operations
	 * of a single empty folder.
	 * @throws IOException
	 */
	@Test
	public void singleFolderTest() throws IOException {
		Path folder = addSingleFolder();
		assertCleanedUpState(1);
		
		deleteSingleFile(folder);
		assertCleanedUpState(0);
	}

	/**
	 * This test verifies the correct add and delete operations
	 * of many empty folders.
	 * @throws IOException
	 */
	@Test
	public void manyFoldersTest() throws IOException {
		int numFolders = 20;
		List<Path> folders = addManyFolders(numFolders);
		assertCleanedUpState(numFolders);
		
		deleteManyFiles(folders);
		assertCleanedUpState(0);
	}
	
	/**
	 * This test verifies the correct add and delete operations
	 * of a single folder containing a single empty folder.
	 * @throws IOException
	 */
	@Test
	public void singleFolderInFolderTest() throws IOException {
		List<Path> folders = addSingleFolderInFolder();
		assertCleanedUpState(2);
		
		deleteSingleFolderInFolder(folders);
		assertCleanedUpState(0);
	}
	
	/**
	 * This test verifies the correct add and delete operations
	 * of a single folder containing many empty folders.
	 * @throws IOException
	 */
	@Test
	public void manyFoldersInFolderTest() throws IOException {
		int nrFolders = 20;
		List<Path> folders = addManyFoldersInFolder(nrFolders);
		assertCleanedUpState(nrFolders + 1);
		
		deleteManyFoldersInFolder(folders);
		assertCleanedUpState(1);
	}
	
	/**
	 * This test verifies the correct add and delete operations
	 * of a single file.
	 * @throws IOException
	 */
	@Test
	public void singleFileTest() throws IOException {
		Path file = addSingleFile();
		assertCleanedUpState(1);
		
		deleteSingleFile(file);
		assertCleanedUpState(0);
	}
	
	/**
	 * This test verifies the correct add and delete operations
	 * of many files.
	 * @throws IOException
	 */
	@Test
	public void manyFilesTest() throws IOException {
		manyFilesTest(100, WAIT_TIME_LONG);
	}

	
	/**
	 * This test verifies the correct add and delete operations
	 * of a very high number of files. This test is used to verify
	 * stable execution under a huge load which implies high CPU time
	 * and memory consumption.
	 * @throws IOException
	 */
	@Test @Ignore
	public void manyFilesStressTest() throws IOException {
		manyFilesTest(10000, WAIT_TIME_STRESSTEST);

	}

	/**
	 * This test verifies the correct add and delete operations
	 * of a single folder containing a single file.
	 * @throws IOException
	 */
	@Test
	public void singleFileInFolderTest() throws IOException {
		List<Path> files = addSingleFileInFolder();
		assertCleanedUpState(2);
		
		deleteSingleFileInFolder(files);
		assertCleanedUpState(0);
	}
	
	/**
	 * This test verifies the correct add and delete operations
	 * of a single folder containing many files.
	 * @throws IOException
	 */
	@Test
	public void manyFilesInFolderTest() throws IOException {
		int nrFiles = 20;
		List<Path> files = addManyFilesInFolder(nrFiles);
		assertCleanedUpState(nrFiles + 1);

		deleteManyFilesInManyFolders(files);
		assertCleanedUpState(0);

	}
	
	/**
	 * This test verifies the correct add and delete operations
	 * of many folders containing many files.
	 * @throws IOException
	 */
	@Test
	public void manyFilesInManyFoldersTest() throws IOException {
		int nrFolders = 10;
		int nrFilesPerFolder = 10;
		
		List<Path> files = addManyFilesInManyFolders(nrFolders, nrFilesPerFolder);
		assertCleanedUpState(nrFolders + nrFolders * nrFilesPerFolder);
		
		deleteManyFilesInManyFolders(files);
		assertCleanedUpState(0);
	}
	
	/**
	 * This test verifies the correct add and delete operations
	 * of many folders containing each a single files. A fail of
	 * this test is mostly implied by incorrect execution order,
	 * i.e. a file cannot be synchronized as the parent folder was
	 * not yet synchronized.
	 * @throws IOException
	 */
	@Test
	public void singleFileInManyFoldersTest() throws IOException{
		int nrFolders = 100;
		List<Path> allPathsInOne = addSingleFileInManyFolders(nrFolders);
		assertCleanedUpState(nrFolders * 2);
		
		deleteManyFilesInManyFolders(allPathsInOne);
		assertCleanedUpState(0);
		
	}
	
	private void manyFilesTest(int nrFiles, int waitTime) throws IOException {
		List<Path> files = addManyFiles(nrFiles, waitTime);
		assertCleanedUpState(nrFiles);
		deleteManyFiles(files);
		assertCleanedUpState(0);
	}
	
	private List<Path> addManyFolders(int numFolders) throws IOException {
		List<Path> folders = FileTestUtils.createRandomFolders(masterRootPath, numFolders);
		waitForExists(folders, WAIT_TIME_LONG);
		return folders;
	}
	
	private List<Path> addSingleFolderInFolder() throws IOException {
		Path folder = FileTestUtils.createRandomFolder(masterRootPath);
		Path subFolder = FileTestUtils.createRandomFolder(folder);
		
		List<Path> folders = new ArrayList<>();
		folders.add(folder);
		folders.add(subFolder);
		
		waitForExists(folders, WAIT_TIME_LONG);
		return folders;
	}
	
	private void deleteSingleFolderInFolder(List<Path> folders) throws IOException {
		ListIterator<Path> it = folders.listIterator(folders.size());
		while(it.hasPrevious()) {
			Path f = it.previous();
			deleteSingleFile(f);
		}
		waitForNotExists(folders, WAIT_TIME_LONG);
	}
	
	private List<Path> addManyFoldersInFolder(int nrSubFolders) throws IOException {
		List<Path> folders = new ArrayList<>();
		
		Path folder = FileTestUtils.createRandomFolder(masterRootPath);
		List<Path> subFolders = FileTestUtils.createRandomFolders(folder, nrSubFolders);
		folders.addAll(subFolders);
		
		waitForExists(folders, WAIT_TIME_LONG);
		return folders;
	}
	
	private void deleteManyFoldersInFolder(List<Path> folders) throws IOException {
		ListIterator<Path> it = folders.listIterator(folders.size());
		while(it.hasPrevious()) {
			Path f = it.previous();
			deleteSingleFile(f);
		}
		
		waitForNotExists(folders, WAIT_TIME_LONG);
	}

	private void deleteSingleFileInFolder(List<Path> files) throws IOException {
		ListIterator<Path> it = files.listIterator(files.size());
		while(it.hasPrevious()) {
			Path f = it.previous();
			deleteSingleFile(f);
		}
		waitForNotExists(files, WAIT_TIME_SHORT);
	}

	private void deleteManyFilesInManyFolders(List<Path> files) throws IOException {
		List<Path> folders = new ArrayList<Path>();
		// delete files
		for(Path p : files) {
			if(Files.isDirectory(p)) {
				logger.debug("Directory: {}", p);
				folders.add(p);
				continue;
			}
			deleteSingleFile(p);
		}
		// delete (now empty) folders
		for(Path p : folders) {
			deleteSingleFile(p);
		}
		
		waitForNotExists(files, WAIT_TIME_LONG);
	}
}
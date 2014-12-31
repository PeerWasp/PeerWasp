package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.junit.Ignore;
import org.junit.Test;
import org.peerbox.utils.FileTestUtils;

public class AddDelete extends FileIntegrationTest {
	
	@Test
	public void singleFolderTest() throws IOException {
		// ADD
		Path folder = addSingleFolder();
		deleteSingleFile(folder);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		assertRootContains(0);
	}
	
	@Test
	public void manyFoldersTest() throws IOException {
		List<Path> folders = addManyFolders();
		deleteManyFiles(folders);
	}

	private List<Path> addManyFolders() throws IOException {
		int numFolders = 20;
		List<Path> folders = FileTestUtils.createRandomFolders(masterRootPath, numFolders);
	
		waitForExists(folders, WAIT_TIME_LONG);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		return folders;
	}
	
	@Test
	public void singleFolderInFolderTest() throws IOException {
		List<Path> folders = addSingleFolderInFolder();
		deleteSingleFolderInFolder(folders);
	}
	
	private List<Path> addSingleFolderInFolder() throws IOException {
		Path folder = FileTestUtils.createRandomFolder(masterRootPath);
		Path subFolder = FileTestUtils.createRandomFolder(folder);
		
		List<Path> folders = new ArrayList<>();
		folders.add(folder);
		folders.add(subFolder);
		
		waitForExists(folders, WAIT_TIME_LONG);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		return folders;
	}
	
	private void deleteSingleFolderInFolder(List<Path> folders) throws IOException {
		ListIterator<Path> it = folders.listIterator(folders.size());
		while(it.hasPrevious()) {
			Path f = it.previous();
			deleteSingleFile(f);
//			Files.delete(f);
		}
		
		waitForNotExists(folders, WAIT_TIME_LONG);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		assertRootContains(0);
	}
	
	@Test
	public void manyFoldersInFolderTest() throws IOException {
		// ADD
		System.out.println("--------------------- Start manyFoldersInFolderTest -------------------------");
		List<Path> folders = addManyFoldersInFolder();
		deleteManyFoldersInFolder(folders);
	}
	
	private List<Path> addManyFoldersInFolder() throws IOException {
		List<Path> folders = new ArrayList<>();
		
		Path folder = FileTestUtils.createRandomFolder(masterRootPath);
		List<Path> subFolders = FileTestUtils.createRandomFolders(folder, 20);
		
//		folders.add(folder);
		folders.addAll(subFolders);
		
		waitForExists(folders, WAIT_TIME_LONG);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		return folders;
	}
	
	private void deleteManyFoldersInFolder(List<Path> folders) throws IOException {
		ListIterator<Path> it = folders.listIterator(folders.size());
		while(it.hasPrevious()) {
			Path f = it.previous();
			deleteSingleFile(f);
//			Files.delete(f);
		}
		
		waitForNotExists(folders, WAIT_TIME_LONG);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		assertRootContains(1);
	}
	
	@Test
	public void singleFileTest() throws IOException {
		// ADD
		Path file = addSingleFile();
		// DELETE
		deleteSingleFile(file);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		assertRootContains(0);
	}
	
//	private void deleteSingleFile(Path file) throws IOException {
//		Files.delete(file);
//		waitForNotExists(file, WAIT_TIME_SHORT);
//		assertSyncClientPaths();
//	}
	
	@Test
	public void manyFilesTest() throws IOException {
		List<Path> files = addManyFiles();
		deleteManyFiles(files);
	}
	
	@Test @Ignore
	public void manyFilesStressTest() throws IOException {
		List<Path> files = addManyFiles(600, WAIT_TIME_STRESSTEST);
		deleteManyFiles(files);

	}
	

	@Test
	public void singleFileInFolderTest() throws IOException {
		// ADD
		System.out.println("START singleFileInFolderTest");
		List<Path> files = addSingleFileInFolder();
		
		// DELETE
		deleteSingleFileInFolder(files);
	}
	
	private void deleteSingleFileInFolder(List<Path> files) throws IOException {
		ListIterator<Path> it = files.listIterator(files.size());
		while(it.hasPrevious()) {
			Path f = it.previous();
//			Files.delete(f);
			deleteSingleFile(f);
		}
		
		waitForNotExists(files, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		assertRootContains(0);
	}

	@Test
	public void manyFilesInFolderTest() throws IOException {
		// ADD
		List<Path> files = addManyFilesInFolder();

		deleteManyFilesInManyFolders(files);
		// DELETE
		
		//sleepMillis(20000);
		
	}
	
	private void deleteManyFilesInFolder(List<Path> files) throws IOException {
		ListIterator<Path> it = files.listIterator(files.size());
		
		while(it.hasPrevious()) {
			Path p = it.previous();
//			Files.delete(p);
			deleteSingleFile(p);
		}
		
		waitForNotExists(files, WAIT_TIME_LONG);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
	}

	@Test
	public void manyFilesInManyFoldersTest() throws IOException {
		List<Path> files = addManyFilesInManyFolders(10);
		deleteManyFilesInManyFolders(files);
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
//			Files.delete(p);
			deleteSingleFile(p);
		}
		
		// delete (now empty) folders
		for(Path p : folders) {
		//	Files.delete(p);
			deleteSingleFile(p);
		}
		
		waitForNotExists(files, WAIT_TIME_LONG);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		assertRootContains(0);
	}

}

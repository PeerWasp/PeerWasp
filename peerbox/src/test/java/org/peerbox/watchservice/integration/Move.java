package org.peerbox.watchservice.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.utils.FileTestUtils;

import com.google.common.io.Files;

public class Move extends FileIntegrationTest{
	
	/**
	 * These tests only consider file and folder contents. The difference 
	 * between delete/create and move is not tested yet!
	 * @throws IOException
	 */
	@Test @Ignore
	public void singleFileMoveTest() throws IOException{
		Path folder = addSingleFolder();
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		waitForExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		
		moveFileOrFolder(srcFile, folder.resolve(srcFile.getFileName()));
		assertSyncClientPaths();
	}

	//still fails
	@Test
	public void manyFilesMoveTest() throws IOException{
		Path folder = addSingleFolder();
		addManyFiles();
		assertSyncClientPaths();
		
		moveManyFilesOrFolders(folder);
		assertSyncClientPaths();
	}
	


	@Test @Ignore
	public void singleEmptyFolderMoveTest() throws IOException{
		Path folderToMove = addSingleFolder();
		Path dstFolder = addSingleFolder();
		
		assertSyncClientPaths();
		
		moveFileOrFolder(folderToMove, dstFolder.resolve(folderToMove.getFileName()));
		assertSyncClientPaths();
	}
	
	@Test @Ignore
	public void singleNonEmptyFolderMoveTest(){
		
	}
	
	@Test @Ignore
	public void manyEmptyFolderMoveTest(){
		
	}
	
	@Test @Ignore
	public void manyNonEmptyFolderMoveTest(){
		
	}
	
	
	private void moveFileOrFolder(Path srcFile, Path dstFile) throws IOException {
		Files.move(srcFile.toFile(), dstFile.toFile());
		waitForExists(dstFile, WAIT_TIME_SHORT);
	}
	
	private void moveManyFilesOrFolders(Path dstFolder) throws IOException {
		File rootFolder = masterRootPath.toFile();
		
		File[] files = rootFolder.listFiles();
		Path lastMovedFile = null;
		for(int i = 0; i < 50; i++){
			if(files[i].isDirectory())
				continue;
			Path dstPath = dstFolder.resolve(files[i].getName());
			System.err.println(dstPath);
			Files.move(files[i], dstPath.toFile());
			lastMovedFile = dstPath;
		}
		waitForExists(lastMovedFile, WAIT_TIME_SHORT);
	}
}

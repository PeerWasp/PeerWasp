package org.peerbox.watchservice.integration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import org.peerbox.utils.FileTestUtils;

import com.google.common.io.Files;

public class Move extends FileIntegrationTest{
	
	/**
	 * These tests only consider file and folder contents. The difference 
	 * between delete/create and move is not tested yet!
	 * @throws IOException
	 */	
	@Test
	public void singleFileMoveTest() throws IOException{
		Path folder = addSingleFolder();
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		waitForExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		
		moveFileOrFolder(srcFile, folder.resolve(srcFile.getFileName()));
		assertSyncClientPaths();
	}
	
	@Test
	public void severalFilesMoveTest() throws IOException{
		Path folder = addSingleFolder();
		List<Path> files = addManyFiles(20, 30);
		waitForExists(files, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		
		moveManyFilesIntoFolder(folder);
		assertSyncClientPaths();
	}

	//still fails
	@Test
	public void manyFilesMoveTest() throws IOException, InterruptedException{
		Path folder = addSingleFolder();
		addManyFiles();
		assertSyncClientPaths();
		moveManyFilesIntoFolder(folder);
		assertSyncClientPaths();
	}
	


	@Test
	public void singleEmptyFolderMoveTest() throws IOException{
		Path folderToMove = addSingleFolder();
		Path dstFolder = addSingleFolder();
		
		assertSyncClientPaths();
		
		moveFileOrFolder(folderToMove, dstFolder.resolve(folderToMove.getFileName()));
		assertSyncClientPaths();
	}
	
	@Test
	public void singleNonEmptyFolderMoveTest() throws IOException{
		Path folderToMove = addSingleFolder();
		Path dstFolderParent = addSingleFolder();
		addSingleFile(folderToMove);
		Path dstFolder = dstFolderParent.resolve("f0");
		Files.move(folderToMove.toFile(), dstFolder.toFile());
		waitForExists(dstFolder, WAIT_TIME_SHORT);
		assertSyncClientPaths();
	}
	
	@Test
	public void manyEmptyFolderMoveTest() throws IOException{
		int nrFolders = 10;
		ArrayList<Path> sources = new ArrayList<Path>();
		Path destination = addSingleFolder();
		for(int i = 0; i < nrFolders; i++){
			sources.add(addSingleFolder());
		}
		Path currentSource = null;
		Path currentDestination = null;
		for(int i = 0; i < nrFolders; i++){
			currentSource = sources.get(i);
			currentDestination = destination.resolve(currentSource.getFileName());
			Files.move(currentSource.toFile(), currentDestination.toFile());
		}
		waitForExists(currentDestination, WAIT_TIME_SHORT);
		assertSyncClientPaths();
	}
	
	@Test
	public void manyNonEmptyFolderMoveTest() throws IOException{
		Path destination = addSingleFolder();
		List<Path> paths = addManyFilesInManyFolders();
		Path lastDestination = null;
		for(Path path: paths){
			if(path.toFile().isDirectory()){
				lastDestination = destination.resolve(path.getFileName());
				if(path.toFile().exists()){
					Files.move(path.toFile(), lastDestination.toFile());
				}
			}
			
		}
		System.out.println("Last destination to wait for: " + lastDestination);
		waitForExists(lastDestination, WAIT_TIME_SHORT);
		assertSyncClientPaths();
	}
	
	
	private void moveFileOrFolder(Path srcFile, Path dstFile) throws IOException {
		Files.move(srcFile.toFile(), dstFile.toFile());
		waitForExists(dstFile, WAIT_TIME_SHORT);
	}
	
	private void moveManyFilesIntoFolder(Path dstFolder) {
		File rootFolder = masterRootPath.toFile();
		
		File[] files = rootFolder.listFiles();
		ArrayList<Path> movedFiles = new ArrayList<Path>();
		int nrMoves = 50;
		for(int i = 0; i < nrMoves; i++){
			if(files[i].isDirectory()){
				nrMoves--;
				continue;
			}
			Path dstPath = dstFolder.resolve(files[i].getName());
			//Path dstPath = Paths.get(files[i].getParentFile().getPath() + File.separator + "X" +  files[i].getName());
			try {
				Files.move(files[i], dstPath.toFile());
				movedFiles.add(dstPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				movedFiles.remove(dstPath);
			}
			

		}
		assertTrue(nrMoves == movedFiles.size());
		for(int i = movedFiles.size()-1; i >= 0; i--){
			waitIfNotExist(movedFiles.get(i), WAIT_TIME_SHORT);
		}
		
	}
	
	
}

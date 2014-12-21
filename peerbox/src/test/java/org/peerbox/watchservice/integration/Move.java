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
import org.peerbox.watchservice.ActionExecutor;

import ch.qos.logback.core.joran.spi.ActionException;

import com.google.common.io.Files;

public class Move extends FileIntegrationTest{
	
	/**
	 * These tests only consider file and folder contents. The difference 
	 * between delete/create and move is not tested yet!
	 * @throws IOException
	 */	
	@Test
	public void singleFileMoveTest() throws IOException{
		logger.debug("Start singleFileMoveTest");
		Path folder = addSingleFolder();
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		waitForExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		moveFileOrFolder(srcFile, folder.resolve(srcFile.getFileName()));
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		logger.debug("End singleFileMoveTest");
	}
	
	
	@Test
	public void singleFileDoubleMoveTest() throws IOException{
		Path folder = addSingleFolder();
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		waitForExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		moveFileOrFolder(srcFile, folder.resolve(srcFile.getFileName()));
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		moveFileOrFolder(folder.resolve(srcFile.getFileName()), srcFile);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
	}
	
	@Test
	public void singleFileDoubleMoveRemoteTest() throws IOException{
		Path localFolder = addSingleFolder();
		Path localSrcFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		Path localDstFile = localFolder.resolve(localSrcFile.getFileName());
		
		waitForExists(localSrcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		moveFileOrFolder(localSrcFile, localDstFile);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		Path remoteFolder = clientRootPath.resolve(localFolder.getFileName());
		Path remoteDestFile = remoteFolder.resolve(localSrcFile.getFileName());
		Path remoteSrcFile = clientRootPath.resolve(localSrcFile.getFileName());
		
		moveFileOrFolder(remoteDestFile, remoteSrcFile);
		waitForExists(localSrcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
	}
	
	@Test
	public void severalFilesMoveTest() throws IOException{
		logger.debug("Start severalFilesMoveTest");
		Path folder = addSingleFolder();
		List<Path> files = addManyFiles(20, 30);
		waitForExists(files, WAIT_TIME_SHORT);
		assertSyncClientPaths();
//		assertQueuesAreEmpty();
		
		moveManyFilesIntoFolder(folder, 10);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		logger.debug("End severalFilesMoveTest");
	}

	//still fails
	@Test
	public void manyFilesMoveTest() throws IOException, InterruptedException{
		logger.debug("Start manyFilesMoveTest");
		Path folder = addSingleFolder();
		addManyFiles();
		assertSyncClientPaths();
//		assertQueuesAreEmpty();
		
		moveManyFilesIntoFolder(folder, 50);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		logger.debug("End manyFilesMoveTest");
	}
	


	@Test
	public void singleEmptyFolderMoveTest() throws IOException{
		logger.debug("Start singleEmptyFolderMoveTest");
		Path folderToMove = addSingleFolder();
		Path dstFolder = addSingleFolder();
		
		assertSyncClientPaths();
//		assertQueuesAreEmpty();
		
		moveFileOrFolder(folderToMove, dstFolder.resolve(folderToMove.getFileName()));
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		logger.debug("End singleEmptyFolderMoveTest");
	}
	
	@Test
	public void singleNonEmptyFolderMoveTest() throws IOException{
		logger.debug("Start singleNonEmptyFolderMoveTest");
		Path folderToMove = addSingleFolder();
		
		Path dstFolderParent = addSingleFolder();
		addSingleFile(folderToMove);
		Path dstFolder = dstFolderParent.resolve(folderToMove.getFileName());
		Files.move(folderToMove.toFile(), dstFolder.toFile());
		waitForExists(dstFolder, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		logger.debug("End singleNonEmptyFolderMoveTest");
	}
	
	@Test
	public void manyEmptyFolderMoveTest() throws IOException{
		logger.debug("Start manyEmptyFolderMoveTest");
		int nrFolders = 10;
		ArrayList<Path> sources = new ArrayList<Path>();
		Path destination = addSingleFolder();
		for(int i = 0; i < nrFolders; i++){
			sources.add(addSingleFolder());
		}
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		Path currentSource = null;
		Path currentDestination = null;
		for(int i = 0; i < nrFolders; i++){
			currentSource = sources.get(i);
			currentDestination = destination.resolve(currentSource.getFileName());
			Files.move(currentSource.toFile(), currentDestination.toFile());
		}
		waitForExists(currentDestination, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		logger.debug("End manyEmptyFolderMoveTest");
	}

	
	@Test
	public void manyNonEmptyFolderMoveTest() throws IOException{
		logger.debug("Start manyNonEmptyFolderMoveTest");
		Path destination = addSingleFolder();
		List<Path> paths = addManyFilesInManyFolders(10);
		
		assertSyncClientPaths();
		assertQueuesAreEmpty();
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
		assertQueuesAreEmpty();
		logger.debug("End manyNonEmptyFolderMoveTest");
	}
	
	@Test @Ignore
	public void localMoveOnRemoteUpdateTest() throws IOException{
		Path folder = addSingleFolder();
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		waitForExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		updateSingleFile(srcFile);
		sleepMillis(2*ActionExecutor.ACTION_WAIT_TIME_MS);
		Path srcOnClient = Paths.get(clientRootPath + File.separator + srcFile.getFileName());
		Path dstOnClient = Paths.get(clientRootPath + File.separator + folder.getFileName() + File.separator + srcFile.getFileName());
		tryToMoveFile(srcOnClient, dstOnClient);
		
		waitForNotExists(srcOnClient, WAIT_TIME_SHORT);
		waitForExists(dstOnClient, WAIT_TIME_SHORT);
		//waitForNotExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
	}
	
	private void tryToMoveFile(Path srcFile, Path dstFile) throws IOException {
		System.out.println("Move " + srcFile + " to " + dstFile);
		Files.move(srcFile.toFile(), dstFile.toFile());
	}
	private void moveFileOrFolder(Path srcFile, Path dstFile) throws IOException {
		System.out.println("Move " + srcFile + " to " + dstFile);
		Files.move(srcFile.toFile(), dstFile.toFile());
		waitForExists(dstFile, WAIT_TIME_SHORT);
	}
	
	private void moveManyFilesIntoFolder(Path dstFolder, int nrMoves) {
		File rootFolder = masterRootPath.toFile();
		
		File[] files = rootFolder.listFiles();
		ArrayList<Path> movedFiles = new ArrayList<Path>();

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

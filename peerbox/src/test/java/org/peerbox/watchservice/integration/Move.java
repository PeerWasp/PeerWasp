package org.peerbox.watchservice.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.peerbox.testutils.FileTestUtils;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;

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
		Path srcFile = addSingleFile(); 
		assertCleanedUpState(2);
		
		moveFileOrFolder(srcFile, folder.resolve(srcFile.getFileName()));
		assertCleanedUpState(2);
	}
	
	
	@Test
	public void singleFileDoubleMoveTest() throws IOException{
		Path folder = addSingleFolder();
		Path srcFile = addSingleFile();
		assertCleanedUpState(2);
		
		moveFileOrFolder(srcFile, folder.resolve(srcFile.getFileName()));
		assertCleanedUpState(2);
		
		moveFileOrFolder(folder.resolve(srcFile.getFileName()), srcFile);
		assertCleanedUpState(2);
	}
	
	@Test
	public void singleFileDoubleMoveRemoteTest() throws IOException{
		Path localFolder = addSingleFolder();
		Path localSrcFile = addSingleFile();
		Path localDstFile = localFolder.resolve(localSrcFile.getFileName());
		
		assertCleanedUpState(2);
		
		moveFileOrFolder(localSrcFile, localDstFile);
		assertCleanedUpState(2);
		
		Path remoteFolder = clientRootPath.resolve(localFolder.getFileName());
		Path remoteDestFile = remoteFolder.resolve(localSrcFile.getFileName());
		Path remoteSrcFile = clientRootPath.resolve(localSrcFile.getFileName());
		
		moveFileOrFolder(remoteDestFile, remoteSrcFile);
		waitForExists(localSrcFile, WAIT_TIME_SHORT);
		assertCleanedUpState(2);
	}
	
	@Test
	public void severalFilesMoveTest() throws IOException{
		Path folder = addSingleFolder();
		List<Path> files = addManyFiles(20, 30);
		assertCleanedUpState(21);
		
		moveManyFilesIntoFolder(files, folder, 10);
		assertCleanedUpState(21);
	}

	@Test
	public void manyFilesMoveTest() throws IOException, InterruptedException{
		Path folder = addSingleFolder();
		List<Path> files = addManyFiles(100, WAIT_TIME_LONG);
		assertCleanedUpState(101);
		
		moveManyFilesIntoFolder(files, folder, 50);
		assertCleanedUpState(101);
	}
	


	@Test
	public void singleEmptyFolderMoveTest() throws IOException{
		Path folderToMove = addSingleFolder();
		Path dstFolder = addSingleFolder();
		assertCleanedUpState(2);
		
		moveFileOrFolder(folderToMove, dstFolder.resolve(folderToMove.getFileName()));
		assertCleanedUpState(2);
	}
	
	@Test
	public void singleNonEmptyFolderMoveTest() throws IOException{
		Path folderToMove = addSingleFolder();
		Path dstFolderParent = addSingleFolder();
		Path dstFolder = dstFolderParent.resolve(folderToMove.getFileName());
		
		addSingleFile(folderToMove);
		assertCleanedUpState(3);
		moveFileOrFolder(folderToMove, dstFolder);
		assertCleanedUpState(3);
	}
	
	@Test
	public void singleFolderWithDesyncedFileMoveTest() throws IOException{
		Path folderToMove = addSingleFolder();
		Path dstFolderParent = addSingleFolder();
		Path dstFolder = dstFolderParent.resolve(folderToMove.getFileName());
		
		addSingleFile(folderToMove);
		Path fileToDesync = addSingleFile(folderToMove);
		assertCleanedUpState(4);
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(fileToDesync);
		
		waitForSynchronized(fileToDesync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(fileToDesync, WAIT_TIME_VERY_SHORT);
		
		moveFileOrFolder(folderToMove, dstFolder);
		assertCleanedUpState(-1, false);
	}
	
	@Test
	public void manyEmptyFolderMoveTest() throws IOException{
		int nrFolders = 10;
		ArrayList<Path> sources = new ArrayList<Path>();
		Path destination = addSingleFolder();
		for(int i = 0; i < nrFolders; i++){
			sources.add(addSingleFolder());
		}

		assertCleanedUpState(nrFolders + 1);
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
	}

	
	@Test
	public void manyNonEmptyFolderMoveTest() throws IOException{
		manyNonEmptyFolderMoveTestFunc();
	}
	
	private void manyNonEmptyFolderMoveTestFunc() throws IOException{
		int nrFolders = 10;
		int nrFilesPerFolder = 10;
		Path destination = addSingleFolder();
		List<Path> paths = addManyFilesInManyFolders(10, 10);
		List<Path> destinationPaths = new ArrayList<Path>();
		int totalFiles = nrFolders + nrFolders * nrFilesPerFolder + 1;
		
		assertCleanedUpState(totalFiles);
		Path lastDestination = null;
		for(Path path: paths){
			if(path.toFile().isDirectory()){
				lastDestination = destination.resolve(path.getFileName());
				if(path.toFile().exists()){
					Files.move(path.toFile(), lastDestination.toFile());
					destinationPaths.add(lastDestination);
				}
			}
			
		}
		waitForExists(destinationPaths, WAIT_TIME_SHORT);
		assertCleanedUpState(totalFiles);
		logger.debug("End manyNonEmptyFolderMoveTest");
	}
	
	
	@Test @Ignore
	public void localMoveOnRemoteUpdateTest() throws IOException{
		Path folder = addSingleFolder();
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		waitForExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
//		updateSingleFile(srcFile);
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
		waitForExists(dstFile, WAIT_TIME_LONG);
	}
	
	private void moveManyFilesIntoFolder(List<Path> files, Path dstFolder, int nrMoves) {
		ArrayList<Path> movedFiles = new ArrayList<Path>();

		for(int i = 0; i < nrMoves; i++){
			if(files.get(i).toFile().isDirectory()){
				nrMoves--;
				continue;
			}
			
			Path dstPath = dstFolder.resolve(files.get(i).getFileName());
			try {
				Files.move(files.get(i).toFile(), dstPath.toFile());
				movedFiles.add(dstPath);
			} catch (IOException e) {
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

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
	public void singleFileMoveWhileUploadTest() throws IOException {
		Path folder = addSingleFolder();
		Path srcFile = addSingleFile(false);
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 4 + 1000);
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
		List<Path> files = addManyFiles(20, WAIT_TIME_SHORT);
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
		List<Path> paths = addManyFolders(2);
		assertCleanedUpState(2);
		
		moveFileOrFolder(paths.get(0), paths.get(1).resolve(paths.get(0).getFileName()));
		assertCleanedUpState(2);
	}
	
	@Test
	public void singleNonEmptyFolderMoveTest() throws IOException{
		List<Path> folders = addManyFolders(2);
		Path dstFolder = folders.get(1).resolve(folders.get(0).getFileName());
		
		addSingleFile(folders.get(0));
		assertCleanedUpState(3);
		moveFileOrFolder(folders.get(0), dstFolder);
		assertCleanedUpState(3);
	}
	
	@Test
	public void singleFolderWithDesyncedFileMoveTest() throws IOException{
//		Path folderToMove = addSingleFolder();
//		Path dstFolderParent = addSingleFolder();
		List<Path> folders = addManyFolders(2);
		Path dstFolder = folders.get(1).resolve(folders.get(0).getFileName());
		
		addSingleFile(folders.get(0));
		Path fileToDesync = addSingleFile(folders.get(0));
		assertCleanedUpState(4);
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(fileToDesync);
		
		waitForSynchronized(fileToDesync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(fileToDesync, WAIT_TIME_VERY_SHORT);
		
		moveFileOrFolder(folders.get(0), dstFolder);
		assertCleanedUpState(-1, false);
	}
	
	@Test
	public void manyEmptyFolderMoveTest() throws IOException{
		int nrFolders = 10;
		List<Path> sources = new ArrayList<Path>();
		Path destination = addSingleFolder();
		sources = addManyFolders(nrFolders);

		assertCleanedUpState(nrFolders + 1);
		Path currentSource = null;
		Path currentDestination = null;
		List<Path> destinations = new ArrayList<Path>(); //null;
		
		for(int i = 0; i < nrFolders; i++){
			currentSource = sources.get(i);
			currentDestination = destination.resolve(currentSource.getFileName());
			Files.move(currentSource.toFile(), currentDestination.toFile());
			destinations.add(currentDestination);
		}
		
		waitForExists(destinations, WAIT_TIME_LONG);
//		assertSyncClientPaths();
//		assertQueuesAreEmpty();
		assertCleanedUpState(nrFolders + 1);
	}

	
	@Test
	public void manyNonEmptyFolderMoveTest() throws IOException{
		manyNonEmptyFolderMoveTestFunc();
	}
	
	@Test @Ignore
	public void simultaneousSingleFileMoveTest() throws IOException{
		Path folder = addSingleFolder();
		Path srcFile = addSingleFile(); 
		Path srcFile2 = addSingleFile();
		Path otherRoot = getNetwork().getRootPaths().get(1);
		Path otherFolder = otherRoot.resolve(folder.getFileName());
		Path resolvedSrc = otherRoot.resolve(srcFile2.getFileName());
		Path resolvedDest = otherFolder.resolve(srcFile.getFileName());
		logger.trace("1. Move from {} to {}", resolvedSrc, resolvedDest);
		logger.trace("2. Move from {} to {}", srcFile, folder.resolve(srcFile.getFileName()));

		assertCleanedUpState(3);
		moveFileOrFolder(resolvedSrc, resolvedDest);
		sleepMillis(2 * ActionExecutor.ACTION_WAIT_TIME_MS / 3);
		moveFileOrFolder(srcFile, folder.resolve(srcFile.getFileName()));
		assertCleanedUpState(3);
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
//		assertSyncClientPaths();
//		assertQueuesAreEmpty();
		assertCleanedUpState(1);
		
//		updateSingleFile(srcFile);
		sleepMillis(2*ActionExecutor.ACTION_WAIT_TIME_MS);
		Path srcOnClient = Paths.get(clientRootPath + File.separator + srcFile.getFileName());
		Path dstOnClient = Paths.get(clientRootPath + File.separator + folder.getFileName() + File.separator + srcFile.getFileName());
		tryToMoveFile(srcOnClient, dstOnClient);
		
		waitForNotExists(srcOnClient, WAIT_TIME_SHORT);
		waitForExists(dstOnClient, WAIT_TIME_SHORT);
		//waitForNotExists(srcFile, WAIT_TIME_SHORT);
//		assertSyncClientPaths();
//		assertQueuesAreEmpty();
		assertCleanedUpState(1);
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
		waitForExists(movedFiles, WAIT_TIME_LONG);
	}

}

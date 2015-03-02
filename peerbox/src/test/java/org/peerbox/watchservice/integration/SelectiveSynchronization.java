package org.peerbox.watchservice.integration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.utils.H2HWaiter;
import org.junit.Ignore;
import org.junit.Test;
import org.peerbox.testutils.FileTestUtils;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;

public class SelectiveSynchronization extends FileIntegrationTest{

	@Test
	public void isFileSynchedByDefaultTest() throws IOException {
		Path path = addFile();
//		assertSyncClientPaths();
//		assertQueuesAreEmpty();
		assertCleanedUpState(1);
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		FileComponent file = eventManager.getFileTree().getFile(path);
		assertTrue(file.isSynchronized());
		
		deleteSingleFile(path, true);
		assertCleanedUpState(0);
	}
	
	@Test
	public void isFileUnsynchedOnDemandTest() throws IOException{
		Path path = addFile();
		assertCleanedUpState(1);
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(path);
		List<Path> toSync = new ArrayList<Path>();
		toSync.add(path);
		waitForSynchronized(toSync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(path, WAIT_TIME_VERY_SHORT);
		assertQueuesAreEmpty();
	}
	
	@Test
	public void createUnsyncedFileTest() throws IOException{
		Path path = addFile();
		assertCleanedUpState(1);
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(path);
		List<Path> toSync = new ArrayList<Path>();
		toSync.add(path);
		waitForSynchronized(toSync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(path, WAIT_TIME_VERY_SHORT);
		
		FileTestUtils.recreateRandomFile(path);

//		Path renamedFile = ConflictHandler.rename(path);
//		toSync.add(renamedFile);
		waitForUpdate(toSync, WAIT_TIME_SHORT);
		waitForExists(toSync, WAIT_TIME_SHORT);
		
		eventManager.onFileSynchronized(path, false);
		assertCleanedUpState(1);
	}
	
	@Test
	public void isFolderUnsynchedOnDemandTest() throws IOException{
		Path path = addFolder();
		assertCleanedUpState(1);
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(path);
		List<Path> toSync = new ArrayList<Path>();
		toSync.add(path);
		waitForSynchronized(toSync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(path, WAIT_TIME_VERY_SHORT);
	}
	
	@Test
	public void isFolderResynchedOnDemandTest() throws IOException{
		Path path = addFolder();
		assertCleanedUpState(1);
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(path);
		List<Path> toSync = new ArrayList<Path>();
		toSync.add(path);
		waitForSynchronized(toSync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(path, WAIT_TIME_VERY_SHORT);
		eventManager.onFileSynchronized(path, true);
		waitForSynchronized(toSync, WAIT_TIME_SHORT, true);
		waitForExists(path, WAIT_TIME_SHORT);
	}
	
	@Test
	public void isFolderSynchedByDefaultTest() throws IOException{
		Path path = addFolder();
		assertCleanedUpState(1);
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		FileComponent file = eventManager.getFileTree().getFile(path);
		assertTrue(file.isSynchronized());
	}
	
	@Test
	public void isFileInUnsyncedFolderUnsyncedByDefaultTest() throws IOException{
		//onFileDesync -> check if unsynced recursively
		List<Path> paths = addSingleFileInFolder();
		assertCleanedUpState(paths.size());
		waitForSynchronized(paths, WAIT_TIME_VERY_SHORT, true);
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(paths.get(0));

		waitForSynchronized(paths, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(paths, WAIT_TIME_VERY_SHORT);
		
	}
	
	@Test @Ignore
	public void isFileInResyncedFolderResyncedByDefaultTest() throws IOException{
		//onFileDesync -> check if unsynced recursively
		List<Path> paths = addSingleFileInFolder();
		assertCleanedUpState(paths.size());
		waitForSynchronized(paths, WAIT_TIME_VERY_SHORT, true);
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(paths.get(0));

		waitForSynchronized(paths, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(paths, WAIT_TIME_VERY_SHORT);
		
		eventManager.onFileSynchronized(paths.get(0), true);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		waitForExists(paths, WAIT_TIME_VERY_SHORT);
		waitForSynchronized(paths, WAIT_TIME_VERY_SHORT, true);

	}
	
	protected void waitForSynchronized(List<Path> paths, int seconds, boolean sync) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!allPathsAreSynchronized(paths, sync));
	}

	private boolean allPathsAreSynchronized(List<Path> paths, boolean sync) {
		// TODO Auto-generated method stub
		IFileTree fileTree = getNetwork().getClients().get(0).getFileEventManager().getFileTree();
		for(Path path : paths){
			if(sync){
				if(fileTree.getFile(path) != null && !fileTree.getFile(path).isSynchronized()){
					return false;
				}
			} else {
				if(fileTree.getFile(path) != null && fileTree.getFile(path).isSynchronized()){
					return false;
				}
			}

		}
		return true;
	}
}

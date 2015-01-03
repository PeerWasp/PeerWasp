package org.peerbox.watchservice.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.utils.H2HWaiter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.peerbox.FileManager;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.FileEventManagerTest;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;

public class SelectiveSynchronization extends FileIntegrationTest{

	@Test
	public void isFileSynchedByDefaultTest() throws IOException {
		Path path = addSingleFile();
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		FileComponent file = eventManager.getFileTree().getFile(path);
		assertTrue(file.getIsSynchronized());
		
		deleteSingleFile(path);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
	}
	
	@Test
	public void isFileUnsynchedOnDemandTest() throws IOException{
		Path path = addSingleFile();
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(path);
		List<Path> toSync = new ArrayList<Path>();
		toSync.add(path);
		waitForSynchronized(toSync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(path, WAIT_TIME_VERY_SHORT);
	}
	
	@Test
	public void isFolderUnsynchedOnDemandTest() throws IOException{
		Path path = addSingleFolder();
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		eventManager.onFileDesynchronized(path);
		List<Path> toSync = new ArrayList<Path>();
		toSync.add(path);
		waitForSynchronized(toSync, WAIT_TIME_SHORT, false);
		waitForNotExistsLocally(path, WAIT_TIME_VERY_SHORT);
	}
	
	@Test
	public void isFolderResynchedOnDemandTest() throws IOException{
		Path path = addSingleFolder();
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
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
		Path path = addSingleFolder();
		assertSyncClientPaths();
		assertQueuesAreEmpty();
		
		FileEventManager eventManager = getNetwork().getClients().get(0).getFileEventManager();
		FileComponent file = eventManager.getFileTree().getFile(path);
		assertTrue(file.getIsSynchronized());
	}
	
	@Test
	public void isFileInUnsyncedFolderUnsyncedByDefaultTest() throws IOException{
		//onFileDesync -> check if unsynced recursively
		List<Path> paths = addSingleFileInFolder();
		waitForExists(paths, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		assertQueuesAreEmpty();
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
		assertSyncClientPaths();
		assertQueuesAreEmpty();
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
	
	protected void waitForSynchronized(Path path, int seconds, boolean sync) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathIsSynchronized(path, sync));
	}
	
	protected boolean pathIsSynchronized(Path path, boolean sync){
		FileTree fileTree = getNetwork().getClients().get(0).getFileEventManager().getFileTree();
		if(sync){
			if(fileTree.getFile(path) != null && !fileTree.getFile(path).getIsSynchronized()){
				return false;
			}
		} else {
			if(fileTree.getFile(path) != null && fileTree.getFile(path).getIsSynchronized()){
				return false;
			}
		}
		return true;
	}
	

	private boolean allPathsAreSynchronized(List<Path> paths, boolean sync) {
		// TODO Auto-generated method stub
		FileTree fileTree = getNetwork().getClients().get(0).getFileEventManager().getFileTree();
		for(Path path : paths){
			if(sync){
				if(fileTree.getFile(path) != null && !fileTree.getFile(path).getIsSynchronized()){
					return false;
				}
			} else {
				if(fileTree.getFile(path) != null && fileTree.getFile(path).getIsSynchronized()){
					return false;
				}
			}

		}
		return true;
	}
}

package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.peerbox.FileManager;
/**
 * 
 * @author Claudio
 * This test creates a new directory in the user's home directory, where some files are created for
 * test purposes. The directory and the files are deleted after the execution of the testcases. This class
 * tests if the events triggered by the FolderWatchService are correctly aggregated and delivered to the H2H
 * framework, whereas the FileManager is mocked to decouple the test from the filesharing library
 */
public class FileEventManagerTest {

	private static int nrFiles = 2;
	private static FileEventManager manager;
	private static String parentPath = System.getProperty("user.home") + File.separator + "PeerBox_FileEventManagerTest" + File.separator; 
	private static File testDirectory;
	private static ArrayList<String> filePaths = new ArrayList<String>();
	private static ArrayList<File> files = new ArrayList<File>();
	
	@Mock
	private FileManager fileManager;
	
	/**
	 * Create the test directory and the files.
	 */
	@BeforeClass
	public static void staticSetup(){
		testDirectory = new File(parentPath);
		testDirectory.mkdir();
		try {
			for(int i = 0; i < nrFiles; i++){
				filePaths.add(parentPath + "file" + i + ".txt");
				files.add(new File(filePaths.get(i)));
				files.get(i).createNewFile();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		manager = new FileEventManager();
	}
	
	/**
	 * Delete the test directory and the files.
	 */
	@AfterClass
	public static void rollback(){
		for(int i = 0; i < nrFiles; i++){
			files.get(i).delete();
		}
		assertTrue(testDirectory.delete());
	}
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		manager.setFileManager(fileManager);
	}
	
	/**
	 * Triggers a create event and a modify event on the same file. Ensures
	 * that only one element is stored in the action queue. This element
	 * has to be still in the create state after this two events are processed.
	 */
	@Test
	public void onFileCreatedTest(){
		
		long start = System.currentTimeMillis();
		
		manager.onFileCreated(Paths.get(filePaths.get(0)));
		BlockingQueue<Action> actionsToCheck = manager.getActionQueue();
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof CreateState);
		
		manager.onFileModified(Paths.get(filePaths.get(0)));
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof CreateState);
		
		//check if the testcase was run in time
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		
		sleepActionWaitTimeMsTwice();
	}
	
	/**
	 * This test simulates a create event and waits ActionExecutor.ACTION_WAIT_TIME_MS amount of
	 * time for the event to be handled. After that, a move is simulated using a delete event on
	 * the same file and a create event on a new file with the same content (but different name).
	 */
	@Test
	public void fromDeleteToModifyTest(){
		//handle artificial create event, wait for handling
		manager.onFileCreated(Paths.get(filePaths.get(0)));
		sleepActionWaitTimeMsTwice();
		
		//check if exactly one element exists in the queue
		BlockingQueue<Action> actionsToCheck = manager.getActionQueue();
		assertTrue(actionsToCheck.size() == 0);
		
		//initiate delete event
		long start = System.currentTimeMillis();
		manager.onFileDeleted(Paths.get(filePaths.get(0)));
		assertTrue(actionsToCheck.size() == 1);
		System.out.println(actionsToCheck.peek().getCurrentState().getClass().toString());
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof DeleteState);
		
		//initiate re-creation, ensure that all happens in time
		manager.onFileCreated(Paths.get(filePaths.get(1)));
		System.out.println("Size: " + actionsToCheck.size());
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof MoveState);
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		sleepActionWaitTimeMsTwice();
	}
	
	/**
	 * Simulate a file delete and an additional modify event, check if the file
	 * remains in the delete state and only one action is stored in the queue.
	 */
	@Test
	public void onFileDeletedTest(){
		long start = System.currentTimeMillis();
		
		manager.onFileDeleted(Paths.get(filePaths.get(0)));
		BlockingQueue<Action> actionsToCheck = manager.getActionQueue();
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof DeleteState);
		
		manager.onFileModified(Paths.get(filePaths.get(0)));
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof DeleteState);
		
		
		//check if the testcase was run in time
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		
		sleepActionWaitTimeMsTwice();
	}
	
	/**
	 * Wait twice the defined time interval to ensure a fully cleaned action queue
	 */
	private void sleepActionWaitTimeMsTwice(){
		try {
			Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

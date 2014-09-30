package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.peerbox.watchservice.states.LocalCreateState;
import org.peerbox.watchservice.states.LocalDeleteState;
import org.peerbox.watchservice.states.InitialState;
import org.peerbox.watchservice.states.LocalUpdateState;
import org.peerbox.watchservice.states.LocalMoveState;
/**
 * 
 * @author Claudio
 * This test creates a new directory in the user's home directory, where some files are created for
 * test purposes. The directory and the files are deleted after the execution of the testcases. This class
 * tests if the events triggered by the FolderWatchService are correctly aggregated and delivered to the H2H
 * framework, whereas the FileManager is mocked to decouple the test from the filesharing library
 */
public class FileEventManagerTest {

	private static int nrFiles = 6;
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
		manager = new FileEventManager(Paths.get(parentPath));
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
		BlockingQueue<FileComponent> fileComponentsToCheck = manager.getFileComponentQueue();
		
		long start = System.currentTimeMillis();
		
		manager.onLocalFileCreated(Paths.get(filePaths.get(0)), false);
		assertTrue(fileComponentsToCheck.size() == 1);
		assertTrue(fileComponentsToCheck.peek().getAction().getCurrentState() instanceof LocalCreateState);
		
		manager.onLocalFileModified(Paths.get(filePaths.get(0)));
		assertTrue(fileComponentsToCheck.size() == 1);
		assertTrue(fileComponentsToCheck.peek().getAction().getCurrentState() instanceof LocalCreateState);
		
		//check if the testcase was run in time
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
	}
	
	/**
	 * This test simulates a create event and waits ActionExecutor.ACTION_WAIT_TIME_MS amount of
	 * time for the event to be handled. After that, a move is simulated using a delete event on
	 * the same file and a create event on a new file with the same content (but different name).
	 */
	@Test
	public void fromDeleteToModifyTest(){
		//handle artificial create event, wait for handling
		manager.onLocalFileCreated(Paths.get(filePaths.get(0)), false);
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		
		//check if exactly one element exists in the queue
		BlockingQueue<FileComponent> actionsToCheck = manager.getFileComponentQueue();
		assertTrue(actionsToCheck.size() == 0);
		
		//initiate delete event
		long start = System.currentTimeMillis();
		
		manager.onLocalFileDeleted(Paths.get(filePaths.get(0)));
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalDeleteState);
		
		//initiate re-creation, ensure that all happens in time
		manager.onLocalFileCreated(Paths.get(filePaths.get(1)), false);
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalMoveState);
		
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
	}
	
	/**
	 * Simulate a file delete and an additional modify event, check if the file
	 * remains in the delete state and only one action is stored in the queue.
	 */
	@Test
	public void onFileDeletedTest(){
		BlockingQueue<FileComponent> actionsToCheck = manager.getFileComponentQueue();
		
		manager.onLocalFileCreated(Paths.get(filePaths.get(0)), false);
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		
		long start = System.currentTimeMillis();
		
		manager.onLocalFileDeleted(Paths.get(filePaths.get(0)));

		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalDeleteState);

		manager.onLocalFileModified(Paths.get(filePaths.get(0)));
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalDeleteState);
		
		//check if the testcase was run in time
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
	}
	
	/**
	 * This test issues several modify events for the same file over a long
	 * period to check if the events are aggregated accordingly.
	 */
	@Test
	public void onFileModifiedTest(){
		BlockingQueue<FileComponent> actionsToCheck = manager.getFileComponentQueue();
		
		long start = System.currentTimeMillis();
		
		manager.onLocalFileCreated(Paths.get(filePaths.get(0)), false);
		manager.onLocalFileModified(Paths.get(filePaths.get(0)));
		assertTrue(actionsToCheck.size() == 1);
		assertNotNull(actionsToCheck);
		assertNotNull(actionsToCheck.peek());
		assertNotNull(actionsToCheck.peek().getAction().getCurrentState());
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalCreateState); //no null pointers should occur anymore here
		
		long end = System.currentTimeMillis();
		
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);

		//issue continuous modifies over a period longer than the wait time
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		manager.onLocalFileModified(Paths.get(filePaths.get(0)));
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		manager.onLocalFileModified(Paths.get(filePaths.get(0)));
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalUpdateState);
		assertTrue(actionsToCheck.size() == 1);
		
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
	}
	
	/**
	 * Advanced test with four files and different events on them.
	 * 
	 * The different events:
	 * - modify file0
	 * - create file1
	 * - delete file0
	 * - modify file2
	 * - delete file2 
	 * - create file3 > move from file2 to file3
	 * 
	 * Expected action queue content
	 * 
	 * (tail) [move file2 to file3], [delete file0] [create file1] (head)
	 */
	
	@Test
	public void multipleFilesTest(){
		//measure start time to ensure the testcase runs before the queue is processed

		BlockingQueue<FileComponent> actionsToCheck = manager.getFileComponentQueue();
		
		//issue all the events, check state of head and if the action corresponds to the correct file
		manager.onLocalFileCreated(Paths.get(filePaths.get(0)), false);
		//manager.onFileCreated(Paths.get(filePaths.get(1)), false);
		manager.onLocalFileCreated(Paths.get(filePaths.get(2)), false);
		//manager.onFileCreated(Paths.get(filePaths.get(3)), false);
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		long start = System.currentTimeMillis();
		manager.onLocalFileModified(Paths.get(filePaths.get(0)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalUpdateState);
		assertTrue(actionsToCheck.peek().getAction().getFilePath().toString().equals(filePaths.get(0)));
		
		manager.onLocalFileCreated(Paths.get(filePaths.get(1)), false);
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 2);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalUpdateState);
		assertTrue(actionsToCheck.peek().getAction().getFilePath().toString().equals(filePaths.get(0)));
		
		
		manager.onLocalFileDeleted(Paths.get(filePaths.get(0)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 2);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalCreateState);
		assertTrue(actionsToCheck.peek().getAction().getFilePath().toString().equals(filePaths.get(1)));
		
		
		manager.onLocalFileModified(Paths.get(filePaths.get(2)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 3);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalCreateState);
		assertTrue(actionsToCheck.peek().getAction().getFilePath().toString().equals(filePaths.get(1)));
		
		
		manager.onLocalFileDeleted(Paths.get(filePaths.get(2)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 3);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalCreateState);
		assertTrue(actionsToCheck.peek().getAction().getFilePath().toString().equals(filePaths.get(1)));
		
		manager.onLocalFileCreated(Paths.get(filePaths.get(3)), false);
		sleepMillis(10);

		assertTrue(actionsToCheck.size() == 3);
		assertTrue(actionsToCheck.peek().getAction().getCurrentState() instanceof LocalCreateState);
		assertTrue(actionsToCheck.peek().getAction().getFilePath().toString().equals(filePaths.get(1)));
		
		List<FileComponent> actionsList = new ArrayList<FileComponent>(actionsToCheck);

		//poll elements from the queue, check state and file path for each of them
		FileComponent head = actionsList.get(0);
		assertTrue(actionsToCheck.size() == 3);
		assertTrue(head.getAction().getCurrentState() instanceof LocalCreateState);
		assertTrue(head.getAction().getFilePath().toString().equals(filePaths.get(1)));
		
		head = actionsList.get(1);
		assertTrue(head.getAction().getCurrentState() instanceof LocalDeleteState);
		assertTrue(head.getAction().getFilePath().toString().equals(filePaths.get(0)));
		
		head = actionsList.get(2);
		assertTrue(head.getAction().getCurrentState() instanceof LocalMoveState);
		assertTrue(head.getAction().getFilePath().toString().equals(filePaths.get(3)));
		
		long end = System.currentTimeMillis();
		
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);	
		
		
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
	}
	
	/**
	 * This test simulates the the process of creating AND moving/renaming a file
	 * before the upload to the network was triggered. Therefore, the old file should
	 * be ignored (initial state, where execute does nothing) and the new file should
	 * be pushed as a create.
	 */
	
	@Test
	public void createOnLocalMove(){

		//sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS*3);
		long start = System.currentTimeMillis();
		
		BlockingQueue<FileComponent> actionsToCheck = manager.getFileComponentQueue();
		assertTrue(actionsToCheck.size() == 0);
		
		manager.onLocalFileCreated(Paths.get(filePaths.get(4)), false);
		sleepMillis(10);
		
		//move the file LOCALLY
		manager.onLocalFileDeleted(Paths.get(filePaths.get(4)));
		sleepMillis(10);
		
		manager.onLocalFileCreated(Paths.get(filePaths.get(5)), false);
		
		FileComponent head = actionsToCheck.peek();
		assertTrue(head.getAction().getCurrentState() instanceof LocalCreateState);
		assertTrue(head.getAction().getFilePath().toString().equals(filePaths.get(5)));
		
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
	}

	/**
	 * Wait the defined time interval. Useful to guarantee different timestamps in
	 * milliseconds if events are programatically created. Furthermore allows to wait
	 * for a cleaned action queue if ActionExecutor.ACTION_TIME_TO_WAIT * 2 is passed
	 * as millisToSleep
	 */
	public static void sleepMillis(long millisToSleep){
		try {
			Thread.sleep(millisToSleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

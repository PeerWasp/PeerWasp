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
		manager.onFileCreated(Paths.get(filePaths.get(0)));
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		
		//check if exactly one element exists in the queue
		BlockingQueue<Action> actionsToCheck = manager.getActionQueue();
		assertTrue(actionsToCheck.size() == 0);
		
		//initiate delete event
		long start = System.currentTimeMillis();
		manager.onFileDeleted(Paths.get(filePaths.get(0)));
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof DeleteState);
		
		//initiate re-creation, ensure that all happens in time
		manager.onFileCreated(Paths.get(filePaths.get(1)));
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof MoveState);
		
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
		
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
	}
	
	/**
	 * This test issues several modify events for the same file over a long
	 * period to check if the events are aggregated accordingly.
	 */
	@Test
	public void onFileModifiedTest(){
		long start = System.currentTimeMillis();
		
		manager.onFileModified(Paths.get(filePaths.get(0)));
		BlockingQueue<Action> actionsToCheck = manager.getActionQueue();
		assertTrue(actionsToCheck.size() == 1);
		assertNotNull(actionsToCheck);
		assertNotNull(actionsToCheck.peek());
		assertNotNull(actionsToCheck.peek().getCurrentState());
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof ModifyState); //Occasionally null pointer exception
		
		long end = System.currentTimeMillis();
		assertTrue(end - start <= ActionExecutor.ACTION_WAIT_TIME_MS);
		
		//issue continuous modifies over a period longer than the wait time
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		manager.onFileModified(Paths.get(filePaths.get(0)));
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		manager.onFileModified(Paths.get(filePaths.get(0)));
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof ModifyState);
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
		long start = System.currentTimeMillis();
		BlockingQueue<Action> actionsToCheck = manager.getActionQueue();
		
		//issue all the events, check state of head and if the action corresponds to the correct file
		manager.onFileModified(Paths.get(filePaths.get(0)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 1);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof ModifyState);
		assertTrue(actionsToCheck.peek().getFilePath().toString().equals(filePaths.get(0)));
		
		manager.onFileCreated(Paths.get(filePaths.get(1)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 2);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof ModifyState);
		assertTrue(actionsToCheck.peek().getFilePath().toString().equals(filePaths.get(0)));
		
		
		manager.onFileDeleted(Paths.get(filePaths.get(0)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 2);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof CreateState);
		assertTrue(actionsToCheck.peek().getFilePath().toString().equals(filePaths.get(1)));
		
		
		manager.onFileModified(Paths.get(filePaths.get(2)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 3);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof CreateState);
		assertTrue(actionsToCheck.peek().getFilePath().toString().equals(filePaths.get(1)));
		
		
		manager.onFileDeleted(Paths.get(filePaths.get(2)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 3);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof CreateState);
		assertTrue(actionsToCheck.peek().getFilePath().toString().equals(filePaths.get(1)));
		
		manager.onFileCreated(Paths.get(filePaths.get(3)));
		sleepMillis(10);
		assertTrue(actionsToCheck.size() == 3);
		assertTrue(actionsToCheck.peek().getCurrentState() instanceof CreateState);
		assertTrue(actionsToCheck.peek().getFilePath().toString().equals(filePaths.get(1)));
		
		List<Action> actionsList = new ArrayList<Action>(actionsToCheck);

		//poll elements from the queue, check state and file path for each of them
		Action head = actionsList.get(0);
		assertTrue(actionsToCheck.size() == 3);
		assertTrue(head.getCurrentState() instanceof CreateState);
		assertTrue(head.getFilePath().toString().equals(filePaths.get(1)));
		
		head = actionsList.get(1);
		assertTrue(head.getCurrentState() instanceof DeleteState);
		assertTrue(head.getFilePath().toString().equals(filePaths.get(0)));
		
		head = actionsList.get(2);
		assertTrue(head.getCurrentState() instanceof MoveState);
		assertTrue(head.getFilePath().toString().equals(filePaths.get(3)));
		
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
	public void remoteCreateOnLocalMove(){
		
//		Class<?> c = manager.getClass();
//		try {
//			Field map = c.getDeclaredField("filePathToAction");
//			map.setAccessible(true);
//			Map<Path, Action> filePathToAction = (HashMap<Path, Action>) map.get(manager);
//			for(Path p: filePathToAction.keySet()){
//				System.out.println("Entry: " + filePathToAction.get(p).getFilePath() + " " + filePathToAction.get(p).getCurrentState());
//			}
//		} catch (NoSuchFieldException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		sleepMillis(ActionExecutor.ACTION_WAIT_TIME_MS*3);
		long start = System.currentTimeMillis();
		BlockingQueue<Action> actionsToCheck = manager.getActionQueue();
		assertTrue(actionsToCheck.size() == 0);
		
		manager.onFileCreated(Paths.get(filePaths.get(4)));
		sleepMillis(10);
		
		//move the file LOCALLY
		manager.onFileDeleted(Paths.get(filePaths.get(4)));
		sleepMillis(10);
		manager.onFileCreated(Paths.get(filePaths.get(5)));
		
		List<Action> actionsList = new ArrayList<Action>(actionsToCheck);
		
		Action head = actionsList.get(0);
		assertTrue(head.getCurrentState() instanceof InitialState);
		assertTrue(head.getFilePath().toString().equals(filePaths.get(4)));
		
		head = actionsList.get(1);
		assertTrue(head.getCurrentState() instanceof CreateState);
		assertTrue(head.getFilePath().toString().equals(filePaths.get(5)));
		
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

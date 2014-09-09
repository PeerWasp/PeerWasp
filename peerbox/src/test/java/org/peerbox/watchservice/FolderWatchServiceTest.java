package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.security.EncryptionUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWatchServiceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(FolderWatchServiceTest.class);
	
	private static Path basePath;
	private FolderWatchService watchService;
	
	private static final int SLEEP_TIME = 1000;
	private static final int FILE_SIZE = 5*1024*1024;
	
	@Mock
	private IFileEventListener fileEventListener;
	
	/**
	 * !! NOTE: need to call start() watchservice in each test !!
	 */
	
	@BeforeClass
	public static void setup() {
		basePath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerBox_FolderWatchServiceTest");
		basePath.toFile().mkdir();
		logger.info("Path: {}", basePath);
	}
	
	@Before
	public void initialization() throws Exception {
		FileUtils.cleanDirectory(basePath.toFile());
		
		MockitoAnnotations.initMocks(this);
		
		watchService = new FolderWatchService(basePath);
		watchService.addFileEventListener(fileEventListener);
	}
	
	@After
	public void cleanup() throws Exception {
		watchService.stop();
		FileUtils.cleanDirectory(basePath.toFile());
	}
	
	@Test
	public void testFileAddEvent() throws Exception {
		watchService.start();
		
		// new file
		Path add = Paths.get(basePath.toString(), "add.txt");
		assertTrue(add.toFile().createNewFile());
		
		sleep();
		// expect 1 event
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileCreated(add);
	}
	
	@Test
	public void testFileModifyEvent() throws Exception {
		// new file
		Path modify = Paths.get(basePath.toString(), "modify.txt");
		assertTrue(modify.toFile().createNewFile());
		
		watchService.start();
		
		// write some content
		FileWriter out = new FileWriter(modify.toFile());
		WatchServiceTestHelpers.writeRandomData(out, FILE_SIZE);
		out.close();
		
		sleep();
		// expect multiple events
		Mockito.verify(fileEventListener, Mockito.atLeastOnce()).onFileModified(modify);
	}
	
	@Test
	public void testFileDeleteEvent() throws Exception {
		// new file
		Path delete = Paths.get(basePath.toString(), "delete.txt");
		assertTrue(delete.toFile().createNewFile());
		// write some content
		FileWriter out = new FileWriter(delete.toFile());
		WatchServiceTestHelpers.writeRandomData(out,  FILE_SIZE);
		out.close();
		
		watchService.start();
		
		// delete
		assertTrue(delete.toFile().delete());
		sleep();
		// expect 1 event
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileDeleted(delete);
	}
	
	@Test
	public void testFileMoveEvent() throws Exception {
		// new file and directory
		Path move = Paths.get(basePath.toString(), "move.txt");
		assertTrue(move.toFile().createNewFile());
		Path newDir = Paths.get(basePath.toString(), "newlocation");
		assertTrue(newDir.toFile().mkdir());
		// write some content
		FileWriter out = new FileWriter(move.toFile());
		WatchServiceTestHelpers.writeRandomData(out,  FILE_SIZE);
		out.close();
		watchService.start();
		
		// move the file into directory
		Path dstFile = Paths.get(newDir.toString(), move.getFileName().toString());
		FileUtils.moveFile(move.toFile(), dstFile.toFile());
		sleep();
		// expect 1 delete, 1 create event
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileDeleted(move);
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileCreated(dstFile);
	}
	
	
	@Test
	public void testFileRenameEvent() throws Exception {
		// new file
		Path rename = Paths.get(basePath.toString(), "rename.txt");
		assertTrue(rename.toFile().createNewFile());
		// write some content
		FileWriter out = new FileWriter(rename.toFile());
		WatchServiceTestHelpers.writeRandomData(out,  FILE_SIZE);
		out.close();
		watchService.start();
		
		// rename the file
		Path newName = Paths.get(basePath.toString(), "rename_new.txt");
		assertTrue(rename.toFile().renameTo(newName.toFile()));
		sleep();
		// expect 1 delete, 1 create event
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileDeleted(rename);
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileCreated(newName);
	}
	
	@Test
	public void testFileCopyEvent() throws Exception {
		// new file
		Path original = Paths.get(basePath.toString(), "copy.txt");
		assertTrue(original.toFile().createNewFile());
		// write some content
		FileWriter out = new FileWriter(original.toFile());
		WatchServiceTestHelpers.writeRandomData(out,  FILE_SIZE);
		out.close();
		watchService.start();
		
		// rename the file
		Path copy = Paths.get(basePath.toString(), "copy_of_file.txt");
		FileUtils.copyFile(original.toFile(), copy.toFile());
		sleep();
		// expect 1 delete, 1 create event
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileCreated(copy);
	}
	
	@Test
	public void testFolderAddEvent() throws Exception {
		Path newFolder = Paths.get(basePath.toString(), "newfolder");
		
		watchService.start();
		assertTrue(newFolder.toFile().mkdir());
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(1)).onFileCreated(newFolder);
	}
	
	@Test
	public void testAddFileInNewFolderEvent() throws Exception {
		Path newFolder = Paths.get(basePath.toString(), "newfolder");
		Path newFile = Paths.get(newFolder.toString(), "file.txt");
		watchService.start();
		
		assertTrue(newFolder.toFile().mkdir());
		assertTrue(newFile.toFile().createNewFile());
		
		sleep();
//		Mockito.verify(fileEventListener, Mockito.times(1)).onFileCreated(newFolder);
//		Mockito.verify(fileEventListener, Mockito.times(1)).onFileCreated(newFile);
		
		
	}
	
	@Test 
	public void testFolderDeleteEvent() {
		
	}
	
	@Test 
	public void testFolderMoveEvent() {
		
	}
	
	@Test 
	public void testFolderRenameEvent() {
		
	}
	
	@Test 
	public void testFolderCopyEvent() {
		
	}
	
	private void sleep() throws InterruptedException {
		Thread.sleep(SLEEP_TIME);
	}
	

//	static String path = null;
//	static FolderWatchService watchService;
//	static File testDirectory;
//	static ArrayList<String> filePaths = new ArrayList<String>();
//	static ArrayList<File> files = new ArrayList<File>();
//	
//	@BeforeClass
//	public static void initializeVariables(){
//		path = System.getProperty("user.home");
//		path = path.concat(File.separator + "PeerBox_FolderWatchServiceTest" + File.separator);//.replace("\\", "/") + "/PeerBox_Test"; 
//		System.out.println("Path to create: " + path);
//		testDirectory = new File(path);
//		testDirectory.mkdir();
//		for(int i = 0; i < 3; i++){
//			filePaths.add(path + "File" + i);
//		}
//		System.out.println("start");
//		try {
//			watchService = new FolderWatchService(Paths.get(path));
//			watchService.start();
//			System.out.println("start");
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		//h2hManager = new H2HManager();
//	}
//	
//	
//	
//	@Test @Ignore
//	public void createFileTest(){
//		try {
//			try {
//				String file1Path = path + File.separator + "File1";
//				File file1 = new File(filePaths.get(0));
//				System.out.println(file1Path);
//				file1.createNewFile();
//				files.add(file1);
//				Thread.sleep(1000);
//				System.out.println(watchService.getActionQueue().size());
//				assertTrue(watchService.getActionQueue().size() == 1);
//				assertTrue(watchService.getActionQueue().peek().getCurrentState() instanceof InitialState);
//				Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	/*@AfterClass @Ignore
//	public static void rollback(){
//		try {
//			System.out.println("Rollback");
//			watchService.stop();
//			createFileTestRollback();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}*/
//	
//	private static void createFileTestRollback(){
//		assertTrue(files.get(0).delete());
//	}
//	
//	
//	
//	@Test
//	public void getLastActionTest(){
//		//Path createdFilePath = Paths.get("example_path/file.name");
//	    String createdFilePathString = path.concat(File.separator).concat("created");
//	    Path createFilePath = Paths.get(createdFilePathString);
//	    Map<String, Action> filenameToAction = new HashMap<String, Action>();
//	    Map<String, Action> contenthashToAction = new HashMap<String, Action>();
//	    
//		
//		try {
//			File file = new File(filePaths.get(0));
//			file.createNewFile();
//		    Action fileAction = new Action();
//			contenthashToAction.put(EncryptionUtil.generateMD5Hash(file).toString(), fileAction);
//			filenameToAction.put(filePaths.get(0), fileAction);
//			
//			Class<?> folderWatchService = Class.forName("org.peerbox.watchservice.FolderWatchService");
//			//Constructor<?> constructor = folderWatchService.getConstructor(Path.class);
// 
//
//			
//			Field hashMap = watchService.getClass().getDeclaredField("contenthashToAction");
//			hashMap.setAccessible(true);
//			hashMap.set(watchService, contenthashToAction);
//			
//			Field nameMap = watchService.getClass().getDeclaredField("filenameToAction");
//			nameMap.setAccessible(true);
//			nameMap.set(watchService, filenameToAction);
//			
//			Method method = watchService.getClass().getDeclaredMethod("getLastAction", Kind.class, Path.class);
//			method.setAccessible(true);
//			Action resultAction = (Action)method.invoke(watchService, StandardWatchEventKinds.ENTRY_CREATE, createFilePath);
//			assertNull(resultAction);
//			resultAction = (Action)method.invoke(watchService, StandardWatchEventKinds.ENTRY_MODIFY, Paths.get(filePaths.get(0)));
//			assertNotNull(resultAction);
//			resultAction = (Action)method.invoke(watchService, StandardWatchEventKinds.ENTRY_DELETE, Paths.get(filePaths.get(0)));
//			System.out.println(filePaths.get(0) + " " +  Paths.get(filePaths.get(0)).toString());
//			assertNotNull(resultAction);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	@Test @Ignore
//	public void changeStateTest(){
//
//		try {
//			//Object service = Class.forName("org.peerbox.watchservice.FolderWatchService").newInstance();
//			Method method = watchService.getClass().getDeclaredMethod("changeState", Action.class, Kind.class);
//			method.setAccessible(true);
//			
//			testTransissionsFromCreateState(method);
//			testTransissionsFromDeleteState(method);
//			testTransissionsFromModifyState(method);
//			testTransissionsFromMoveState(method);
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	private void testTransissionsFromCreateState(Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
//		Action createAction = new Action();
//		
//		method.invoke(watchService, createAction, StandardWatchEventKinds.ENTRY_CREATE);
//		assertTrue(createAction.getCurrentState() instanceof CreateState);
//		
//		//test the transissions from the create state
//		method.invoke(watchService, createAction, StandardWatchEventKinds.ENTRY_CREATE);
//		assertTrue(createAction.getCurrentState() instanceof CreateState);
//		
//		method.invoke(watchService, createAction, StandardWatchEventKinds.ENTRY_MODIFY);
//		assertTrue(createAction.getCurrentState() instanceof CreateState);
//		
//		method.invoke(watchService, createAction, StandardWatchEventKinds.ENTRY_DELETE);
//		assertTrue(createAction.getCurrentState() instanceof InitialState);
//	}
//	
//	private void testTransissionsFromDeleteState(Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
//		Action deleteAction = new Action();
//		
//		method.invoke(watchService, deleteAction, StandardWatchEventKinds.ENTRY_DELETE);
//		assertTrue(deleteAction.getCurrentState() instanceof DeleteState);
//		
//		//test transissions from the delete state
//		method.invoke(watchService, deleteAction, StandardWatchEventKinds.ENTRY_DELETE);
//		assertTrue(deleteAction.getCurrentState() instanceof DeleteState);
//		
//		method.invoke(watchService, deleteAction, StandardWatchEventKinds.ENTRY_MODIFY);
//		assertTrue(deleteAction.getCurrentState() instanceof DeleteState);
//		
//		method.invoke(watchService, deleteAction, StandardWatchEventKinds.ENTRY_CREATE);
//		assertTrue(deleteAction.getCurrentState() instanceof MoveState);
//	}
//	
//	private void testTransissionsFromModifyState(Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
//		Action modifyAction = new Action();
//		
//		method.invoke(watchService, modifyAction, StandardWatchEventKinds.ENTRY_MODIFY);
//		assertTrue(modifyAction.getCurrentState() instanceof ModifyState);
//		
//		//test transissions from the modify state
//		method.invoke(watchService, modifyAction, StandardWatchEventKinds.ENTRY_MODIFY);
//		assertTrue(modifyAction.getCurrentState() instanceof ModifyState);
//		
//		method.invoke(watchService, modifyAction, StandardWatchEventKinds.ENTRY_CREATE);
//		assertTrue(modifyAction.getCurrentState() instanceof CreateState);
//		
//		method.invoke(watchService, modifyAction, StandardWatchEventKinds.ENTRY_DELETE);
//		assertTrue(modifyAction.getCurrentState() instanceof DeleteState);
//	}
//	
//	private void testTransissionsFromMoveState(Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
//		Action moveAction = new Action();
//		
//		method.invoke(watchService, moveAction, StandardWatchEventKinds.ENTRY_DELETE);
//		assertTrue(moveAction.getCurrentState() instanceof DeleteState);
//		
//		method.invoke(watchService, moveAction, StandardWatchEventKinds.ENTRY_CREATE);
//		assertTrue(moveAction.getCurrentState() instanceof MoveState);
//		
//		//test transissions from the move state
//		method.invoke(watchService, moveAction, StandardWatchEventKinds.ENTRY_MODIFY);
//		assertTrue(moveAction.getCurrentState() instanceof MoveState);
//		
//		method.invoke(watchService, moveAction, StandardWatchEventKinds.ENTRY_CREATE);
//		assertTrue(moveAction.getCurrentState() instanceof MoveState);
//
//		method.invoke(watchService, moveAction, StandardWatchEventKinds.ENTRY_DELETE);
//		assertTrue(moveAction.getCurrentState() instanceof DeleteState);
//	}
}

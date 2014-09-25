package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.peerbox.FileManager;

//import com.google.common.io.Files;

public class FileWalkerTestWhenFolderCreated {
	
	@Mock
	private FileManager fileManager;
	
	private static String parentPath = System.getProperty("user.home") + File.separator + "PeerBox_FileWalkerTest" + File.separator; 
	
	private static String dir1Str = parentPath + "dir1" + File.separator;
	private static String dir2Str = parentPath + "dir2" + File.separator;
	private static String dir3Str = parentPath + "dir3" + File.separator;
	private static String file1Str = dir1Str + "file1";
	private static String file2Str = dir1Str + "file2";
	private static String dir1NewStr = dir2Str + "dir1";
	private static String file3Str = dir3Str + "file2";
	private static String file4Str = dir3Str + "file2";
	
	private static File testDirectory;
	private static File dir1;
	private static File dir2;
	private static File dir3;
	private static RandomAccessFile randfile1;
	private static File file1;
	private static File file2;
	private static File file3;
	private static File file4;
	private static File dir1New;
	
	private static FileEventManager manager;
	private static FolderWatchService watchService;
	
	@BeforeClass
	public static void staticSetup(){
		testDirectory = new File(parentPath);
		testDirectory.mkdir();
		manager = new FileEventManager(Paths.get(parentPath), false);	
		
		dir1 = new File(dir1Str);
		dir2 = new File(dir2Str);
		dir3 = new File(dir3Str);
		file1 = new File(file1Str);
		file2 = new File(file2Str);
		file3 = new File(file3Str);
		file4 = new File(file4Str);
		dir1New = new File(dir1NewStr);
		dir1.mkdir();
		dir2.mkdir();
		
		try {
			file1.createNewFile();
			randfile1 = new RandomAccessFile(file1Str, "rw");
			randfile1.setLength(1024);
			randfile1.close();
			file2.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			watchService = new FolderWatchService(Paths.get(parentPath));
			watchService.addFileEventListener(manager);
			watchService.start();
			//watchService.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		manager.setFileManager(fileManager);
		try {
			Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void rollback() throws Exception{
		watchService.stop();
		file1.delete();
		file2.delete();
		//dirA.delete();
		dir1.delete();
		dir2.delete();
		testDirectory.delete();

		try {
			FileUtils.deleteDirectory(testDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//assertTrue(testDirectory.delete());
		
	}
	
	/**
	 * This test should detect the following directory move:
	 * 
	 *           root					    root
	 *            |                          |
	 *     ---------------                   ---------
	 *     |             |                           |
	 *    dir1			dir2                        dir2
	 *     |                                         |
	 *     ---------            =>                  dir1  
	 *     |       |                                 |
	 *    file1    file2                             -----------
	 *                                               |         |
	 *                                             file1      file2
	 *														   										           
	 * @throws InterruptedException 
	 *
	 */
	@Test
	public void testFolderMoveDetection() throws InterruptedException{
		try {
			manager.onFileCreated(dir1.toPath(), false);
			manager.onFileCreated(file1.toPath(), false);
			manager.onFileCreated(file2.toPath(), false);
			manager.onFileCreated(dir2.toPath(), false);
			Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
			assertTrue(manager.getDeletedFileComponents().size() == 0);
			assertTrue(manager.getFileComponentQueue().size() == 0);
			
			Files.move(dir1.toPath(), dir1New.toPath());
			Thread.sleep(10);
			
			assertTrue(manager.getDeletedFileComponents().size() == 1);
			assertTrue(manager.getDeletedByContentNamesHash().size() == 1);
			assertTrue(manager.getFileComponentQueue().size() == 2);
			
			//Ensure old files are removed and new ones appended to the tree
			assertNull(manager.getFileTree().getComponent(dir1Str));
			assertNull(manager.getFileTree().getComponent(file1Str));
			assertNull(manager.getFileTree().getComponent(file2Str));
			assertNotNull(manager.getFileTree().getComponent(dir2Str + "dir1"));
			assertNotNull(manager.getFileTree().getComponent(dir2Str  + "dir1" + File.separator + "file1"));
			assertNotNull(manager.getFileTree().getComponent(dir2Str  + "dir1" + File.separator + "file2"));
			
			Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS * 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test @Ignore
	public void testDataDiscovery(){
		dir3.mkdir();
		try {
			file3.createNewFile();
			file4.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

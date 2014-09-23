package org.peerbox.watchservice;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

public class FileWalkerTestWhenFolderCreated {

	private static File testDirectory;
	private static String parentPath = System.getProperty("user.home") + File.separator + "PeerBox_FileWalkerTest" + File.separator; 
	
	private static String dir1Str = parentPath + "dir1" + File.separator;
	private static String dir2Str = parentPath + "dir2" + File.separator;
	private static String file1Str = dir1Str + "file1";
	private static String dirAStr = dir1Str + "dirA" + File.separator;
	private static String file2Str = dirAStr + "file2";
	private static String dir1NewStr = dir2Str + "dir1";
	
	private static File dir1;
	private static File dir2;
	private static File file1;
	private static File file2;
	private static File dirA;
	private static File dir1New;
	
	private static FileEventManager manager;
	private static FolderWatchService watchService;
	
	@BeforeClass
	public static void setup(){
		testDirectory = new File(parentPath);
		testDirectory.mkdir();
		
		dir1 = new File(dir1Str);
		dir2 = new File(dir2Str);
		file1 = new File(file1Str);
		file2 = new File(file2Str);
		dirA = new File(dirAStr);
		dir1New = new File(dir1NewStr);
		
		dir1.mkdir();
		dir2.mkdir();
		dirA.mkdir();
		try {
			file1.createNewFile();
			file2.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager = new FileEventManager(Paths.get(parentPath));
		
		try {
			watchService = new FolderWatchService(Paths.get(parentPath));
			watchService.addFileEventListener(manager);
			//watchService.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




	}
	@AfterClass
	public static void rollback(){

//		file1.delete();
//		file2.delete();
//		dirA.delete();
//		dir1.delete();
//		dir2.delete();
//		testDirectory.delete();

		
//		try {
//			FileUtils.deleteDirectory(testDirectory);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	 *    file1    dirA                              -----------
	 *             |                                 |         |
	 *             file2                            file1      dirA
	 *														   |
	 *												           file2
	 * @throws InterruptedException 
	 *
	 */
	@Test
	public void testFolderMoveDetection() throws InterruptedException{
		try {
			watchService.start();
			Files.move(dir1, dir1New);
			watchService.stop();
			//Thread.sleep(3000);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}

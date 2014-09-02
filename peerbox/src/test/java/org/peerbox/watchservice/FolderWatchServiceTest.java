package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.model.H2HManager;

public class FolderWatchServiceTest {

	static String path = null;
	static FolderWatchService watchService;
	static File testDirectory;
	static ArrayList<String> filePaths = new ArrayList<String>();
	static ArrayList<File> files = new ArrayList<File>();
	
	@BeforeClass
	public static void initializeVariables(){
		path = System.getProperty("user.home");
		path = path.concat(File.separator + "PeerBox_FolderWatchServiceTest" + File.separator);//.replace("\\", "/") + "/PeerBox_Test"; 
		System.out.println("Path to create: " + path);
		testDirectory = new File(path);
		testDirectory.mkdir();
		for(int i = 0; i < 3; i++){
			filePaths.add(path + "File" + i);
		}
		System.out.println("start");
		try {
			watchService = new FolderWatchService(Paths.get(path));
			watchService.start();
			System.out.println("start");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//h2hManager = new H2HManager();
	}
	
	
	
	@Test
	public void createFileTest(){
		try {
			try {
				String file1Path = path + File.separator + "File1";
				File file1 = new File(filePaths.get(0));
				System.out.println(file1Path);
				file1.createNewFile();
				files.add(file1);
				Thread.sleep(1000);
				System.out.println(watchService.getActionQueue().size());
				assertTrue(watchService.getActionQueue().size() == 1);
				assertTrue(watchService.getActionQueue().peek().getCurrentState() instanceof InitialState);
				Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void rollback(){
		try {
			System.out.println("Rollback");
			watchService.stop();
			createFileTestRollback();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static void createFileTestRollback(){
		assertTrue(files.get(0).delete());
	}
	//public static void main(String[] args) throws Exception {
		
		
		
	/*	if(args.length > 1){
			rootPath = args[1];
		}
		FolderWatchService service = new FolderWatchService(Paths.get(path));
		service.start();
		System.out.println("Running");
		*/
//		Thread.sleep(1000*10);
//		service.stop();
//		System.out.println("Stopping");
	//}

}

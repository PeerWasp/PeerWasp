package org.peerbox.watchservice;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.peerbox.FileManager;

public class FileWalkerTest {
	
	private static int nrFiles = 6;
	private static String parentPath = System.getProperty("user.home") + File.separator + "PeerBox_FileWalkerTest" + File.separator; 
	private static File testDirectory;
	private static ArrayList<String> filePaths = new ArrayList<String>();
	private static ArrayList<File> files = new ArrayList<File>();
	private static FileEventManager manager;
	
	@Mock
	private FileManager fileManager;
	
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
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		manager.setFileManager(fileManager);
	}
	
	@AfterClass
	public static void rollback(){
		for(int i = 0; i < nrFiles; i++){
			files.get(i).delete();
		}
		assertTrue(testDirectory.delete());
	}
	
	
	@Test
	public void indexDirectoryRecursivelyTest(){
		FileWalker walker = new FileWalker(Paths.get(parentPath), manager);
		walker.indexDirectoryRecursively();
		
		assertTrue(manager.getActionQueue().size() != 0);
	}
}

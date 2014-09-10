package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.peerbox.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeFolderWatchServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(NativeFolderWatchServiceTest.class);
	private FolderWatchService watchService;
	private FileEventManager eventManager;
	@Mock
	private FileManager fileManager;
	private static Path basePath;
	
	
	private static final int NUM_CHARS_SMALL_FILE = 50*1024;
	private static final int NUM_CHARS_BIG_FILE = 50*1024*1024;
	private static final int SLEEP_TIME = 4000;
	
	@BeforeClass
	public static void setup() throws Exception {
		basePath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerBox_FolderWatchServiceTest");
		basePath.toFile().mkdir();
		logger.info("Path: {}", basePath);
		
	}
	
	@AfterClass
	public static void teardown() {
		
	}
	
	@Before 
	public void initialization() throws Exception {
		FileUtils.cleanDirectory(basePath.toFile());
		
		MockitoAnnotations.initMocks(this);
		
		watchService = new FolderWatchService(basePath);
		eventManager = new FileEventManager();
		eventManager.setFileManager(fileManager);
		watchService.addFileEventListener(eventManager);

		logger.info("Running");
	}
	
	@After
	public void cleanFolder() throws Exception {
		watchService.stop();
		FileUtils.cleanDirectory(basePath.toFile());
	}
	
	private void sleep() throws InterruptedException {
		Thread.sleep(SLEEP_TIME);
	}

	/**
	 * Create a file and test whether it gets handled by the 
	 * handleCreateEvent and eventually send over to H2H.add
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFileCreate() throws Exception {
		watchService.start();
		
		// create new file
		File add = Paths.get(basePath.toString(), "add_empty.txt").toFile();
		add.createNewFile();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).add(add);
	}
	
	/**
	 * Create a file with some data in it and test whether
	 * it gets handled by the handleCreateEvent
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallFileCreate() throws Exception {
		watchService.start();
		
		// create new small file
		File add = Paths.get(basePath.toString(), "add_small.txt").toFile();
		
		FileWriter out = new FileWriter(add);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).add(add);
	}
	
	/**
	 * Create a file which contains a large portion of data and test
	 * whether it gets handled by the handleCreateEvent
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBigFileCreate() throws Exception {
		watchService.start();
		
		// create new big file
		File add = Paths.get(basePath.toString(), "add_big.txt").toFile();
		
		FileWriter out = new FileWriter(add);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).add(add);
	}
	
	/**
	 * Test whether a file which already was added to H2H gets 
	 * deleted successfully after a handleDeleteEvent
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFileDelete() throws Exception {
		// create new file
		File delete = Paths.get(basePath.toString(), "delete_empty.txt").toFile();
		delete.createNewFile();
		sleep();
		
		watchService.start();
		//delete newly created file
		FileUtils.forceDelete(delete);
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).delete(delete);
	}
	
	/**
	 * Test whether a modify event will be detected when a file
	 * gets altered
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFileModify() throws Exception {
		watchService.start();
		
		// create new small file
		File modify = Paths.get(basePath.toString(), "modify_small.txt").toFile();
		
		FileWriter out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).add(modify);
		
		// modify newly created file
		out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).update(modify);
	}

	/**
	 * Test whether a modify event of a big file gets detected when a file
	 *  gets altered
	 *  
	 * @throws Exception
	 */
	@Test
	public void testBigFileModify() throws Exception {
		watchService.start();
		
		// create new small file
		File modify = Paths.get(basePath.toString(), "modify_big.txt").toFile();
		
		FileWriter out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).add(modify);
		
		// modify newly created file
		out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).update(modify);
		
	}
	
	/**
	 * Test whether renaming a file gets recognized as an move event
	 * @throws Exception
	 */
	@Test
	public void testFileRename() throws Exception {
		watchService.start();
	
		File rename = Paths.get(basePath.toString(), "rename.txt").toFile();
		File newName = Paths.get(basePath.toString(), "rename_rename.txt").toFile();
		
		FileWriter out = new FileWriter(rename);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).add(rename);
		
		rename.renameTo(newName);
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).move(rename, newName);
		
	}
	
	/**
	 * Test whether the movement of a file to a sub directory gets recognized
	 * as an move event
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFileMove() throws Exception {
		watchService.start();
		
		File file = Paths.get(basePath.toString(), "move.txt").toFile();
		File newFile = Paths.get(basePath.toString(), "\\subfolder\\move.txt").toFile();
		
		FileWriter out = new FileWriter(file);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).add(file);
		
		FileUtils.moveFile(file, newFile);
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).move(file, newFile);
	}
	
	/**
	 * Copy an existing file to another location and check whether 
	 * the appropriate event gets triggered
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFileCopy() throws Exception {
		watchService.start();
		
		File file = Paths.get(basePath.toString(), "move.txt").toFile();
		File newFile = Paths.get(basePath.toString(), "\\subfolder").toFile();
		
		FileWriter out = new FileWriter(file);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).add(file);
		
		FileUtils.copyFileToDirectory(file, newFile);
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).add(newFile);
	}
	
	public void testBigFileCopy() {
		
	}
	
	@Test
	public void testManySimultaneousEvents() throws IOException {
	}
	


}

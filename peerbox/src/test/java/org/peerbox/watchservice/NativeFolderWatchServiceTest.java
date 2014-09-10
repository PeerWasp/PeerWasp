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

	@Test
	public void testFileCreate() throws Exception {
		watchService.start();
		
		// create new file
		File add = Paths.get(basePath.toString(), "add_empty.txt").toFile();
		add.createNewFile();
		sleep();
		
		Mockito.verify(fileManager, Mockito.times(1)).add(add);
	}
	
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

	@Test
	public void testBigFileModify() {
		
	}
	
	@Test
	public void testFileRename() throws IOException, NoSessionException, NoPeerConnectionException, IllegalFileLocation, InterruptedException {
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
	
	public void testFileMove() {
		
	}
	
	public void testFileCopy() {
		
	}
	
	public void testBigFileCopy() {
		
	}
	
	public void testManySimultaneousEvents() {
		
	}
	


}

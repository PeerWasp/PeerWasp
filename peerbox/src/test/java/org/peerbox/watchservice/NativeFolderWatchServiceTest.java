package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeFolderWatchServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(NativeFolderWatchServiceTest.class);
	private FolderWatchService watchService;
	private FileEventManager eventManager;
	private static Path basePath;
	private static Random rnd;
	
	private static final int NUM_CHARS_SMALL_FILE = 50*1024;
	private static final int NUM_CHARS_BIG_FILE = 5*1024*1024;
	private static final int SLEEP_TIME = 4000;
	
	@BeforeClass
	public static void setup() throws Exception {
		basePath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerBox_FolderWatchServiceTest");
		basePath.toFile().mkdir();
		logger.info("Path: {}", basePath);
		
		rnd = new Random();
	}
	
	@AfterClass
	public static void teardown() {
		
	}
	
	@Before 
	public void initialization() throws Exception {
		watchService = new FolderWatchService(basePath);
		eventManager = new FileEventManager();
		watchService.addFileEventListener(eventManager);
		
		watchService.start();
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
	public void testFileCreate() throws IOException, InterruptedException {
		File add = Paths.get(basePath.toString(), "add_empty.txt").toFile();
		add.createNewFile();
		sleep();
	}
	
	@Test
	public void testSmallFileCreate() throws IOException, InterruptedException {
		File add = Paths.get(basePath.toString(), "add_small.txt").toFile();
		
		FileWriter out = new FileWriter(add);
		writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
	}
	
	@Test
	public void testBigFileCreate() throws IOException, InterruptedException {
		File add = Paths.get(basePath.toString(), "add_big.txt").toFile();
		
		FileWriter out = new FileWriter(add);
		writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();
		sleep();
	}
	
	public void testFileDelete() {
		
	}
	
	public void testFileModify() {
		
	}

	public void testBigFileModify() {
		
	}
	
	public void testFileRename() {
		
	}
	
	public void testFileMove() {
		
	}
	
	public void testFileCopy() {
		
	}
	
	public void testBigFileCopy() {
		
	}
	
	public void testManySimultaneousEvents() {
		
	}
	
	private void writeRandomData(FileWriter out, int numCharacters) throws IOException {
		for(int i = 0; i < numCharacters; ++i) {
			out.write(getRandomCharacter());
			out.flush();
		}
	}
	
	private char getRandomCharacter() {
		char c = (char)(rnd.nextInt(26) + 'a');
		return c;
	}

}

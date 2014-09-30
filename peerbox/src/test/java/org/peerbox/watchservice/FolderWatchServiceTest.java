package org.peerbox.watchservice;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.mockito.internal.matchers.Any;
import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWatchServiceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(FolderWatchServiceTest.class);
	
	private static Path basePath;
	private FolderWatchService watchService;
	
	private static final int SLEEP_TIME = 1000;
	private static final int FILE_SIZE = 1*1024*1024;
	
	@Mock
	private ILocalFileEventListener fileEventListener;
	
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
		watchService = null;
		FileUtils.cleanDirectory(basePath.toFile());
	}
	
	@Test
	public void testServiceStart() throws Exception {
		Path file = addModifyDelete("file_1.txt");
		// service stopped -> no events should be processed
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileCreated(file, false);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileModified(file);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileDeleted(file);
		
		watchService.start();
		
		file = addModifyDelete("file_2.txt");
		// service started -> events should be processed
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(file,false);
		Mockito.verify(fileEventListener, Mockito.atLeastOnce()).onLocalFileModified(file);
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(file);
	}

	@Test
	public void testServiceStop() throws Exception {
		watchService.start();
		sleep();
		watchService.stop();
		
		Path file = addModifyDelete("file_1.txt");
		
		// service stopped -> no events should be processed
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileCreated(file,false);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileModified(file);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileDeleted(file);
	}
	
	@Test
	public void testServiceRestart() throws Exception {
		watchService.start();
		Path file = addModifyDelete("file_1.txt");
		// service started -> events should be processed
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(file,false);
		Mockito.verify(fileEventListener, Mockito.atLeastOnce()).onLocalFileModified(file);
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(file);
		
		watchService.stop();
		file = addModifyDelete("file_2.txt");
		// service stopped -> no events should be processed
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileCreated(file, false);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileModified(file);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileDeleted(file);
		watchService.start();
		// service stopped -> no events should be processed
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileCreated(file, false);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileModified(file);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileDeleted(file);
	}
	
	private Path addModifyDelete(String filename) throws IOException, InterruptedException {
		// file operations
		Path file = Paths.get(basePath.toString(), filename);
		assertTrue(file.toFile().createNewFile());
		sleep();
		
		FileWriter out = new FileWriter(file.toFile());
		WatchServiceTestHelpers.writeRandomData(out, FILE_SIZE);
		out.close();
		sleep();
		
		Files.delete(file);
		sleep();
		return file;
	}

	@Test
	public void testFileAddEvent() throws Exception {
		watchService.start();
		
		// new file
		Path add = Paths.get(basePath.toString(), "add.txt");
		assertTrue(add.toFile().createNewFile());
		
		sleep();
		// expect 1 event
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(add, false);
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
		// expect multiple modify events
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileCreated(modify, false);
		Mockito.verify(fileEventListener, Mockito.atLeastOnce()).onLocalFileModified(modify);
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
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(delete);
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
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(move);
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(dstFile, false);
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
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(rename);
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(newName, false);
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
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(copy, false);
	}
	
	@Test
	public void testFolderAddEvent() throws Exception {
		Path newFolder = Paths.get(basePath.toString(), "newfolder");
		
		watchService.start();
		assertTrue(newFolder.toFile().mkdir());
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(newFolder, false);
	}
	
	@Test
	public void testAddFileInNewFolderEvent() throws Exception {
		Path newFolder = Paths.get(basePath.toString(), "newfolder");
		Path newFile = Paths.get(newFolder.toString(), "file.txt");
		watchService.start();
		
		assertTrue(newFolder.toFile().mkdir());
//		sleep(); //-> this sleep shows that create event is fired if we wait a bit (until folder is registered)
		assertTrue(newFile.toFile().createNewFile());
		
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(newFolder, false);
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(newFile, false);
	}
	
	@Test 
	public void testEmptyFolderDelete() throws Exception {
		// create folder and delete it
		Path folder = Paths.get(basePath.toString(), "todelete");
		Files.createDirectory(folder);
		watchService.start();
		Files.delete(folder);
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(folder);
	}
	
	private List<Path> createFolderWithFiles(Path folder, int numFiles) throws IOException {
		Files.createDirectory(folder);
		List<Path> files = new ArrayList<Path>();
		for(int i = 0; i < numFiles; ++i) {
			// create file with some content
			Path file = Paths.get(folder.toString(), String.format("%s.txt", i));
			Files.createFile(file);
			FileWriter out = new FileWriter(file.toFile());
			WatchServiceTestHelpers.writeRandomData(out,  FILE_SIZE);
			out.close();
			files.add(file);
		}
		return files;
	}
	
	@Test 
	public void testFolderDelete() throws Exception {
		// create folder and some files in it.
		Path folder = Paths.get(basePath.toString(), "todelete");
		List<Path> files = createFolderWithFiles(folder, 200);
				
		watchService.start();
		FileUtils.deleteDirectory(folder.toFile());
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(folder);
		Mockito.verify(fileEventListener, Mockito.times(1+files.size())).onLocalFileDeleted(anyObject());
	}
	
	@Test 
	public void testFolderMoveEvent() throws Exception {
		Path folder = Paths.get(basePath.toString(), "tomove");
		List<Path> files = createFolderWithFiles(folder, 200);
		
		Path newLocation = Paths.get(basePath.toString(), "newlocation");
		Files.createDirectory(newLocation);
		newLocation = Paths.get(newLocation.toString(), "tomove");
		
		watchService.start();
		Files.move(folder, newLocation);
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(folder);
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(newLocation, false);
	}
	
	@Test 
	public void testFolderRenameEvent() throws Exception {
		Path folder = Paths.get(basePath.toString(), "torename");
		List<Path> files = createFolderWithFiles(folder, 200);
		
		Path rename = Paths.get(basePath.toString(), "torename_rename");
		
		watchService.start();
		Files.move(folder, rename);
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileDeleted(folder);
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(rename, false);
	}
	
	@Test 
	public void testFolderCopyEvent() throws Exception {
		Path folder = Paths.get(basePath.toString(), "tomove");
		List<Path> files = createFolderWithFiles(folder, 200);
		
		Path copy = Paths.get(basePath.toString(), "copy");
		
		watchService.start();
		Files.copy(folder, copy);
		sleep();
		// old folder untouched
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileDeleted(folder);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileCreated(folder, false);
		Mockito.verify(fileEventListener, Mockito.never()).onLocalFileModified(folder);
		// new folder
		Mockito.verify(fileEventListener, Mockito.times(1)).onLocalFileCreated(anyObject(), false);
	}
	
	@Test 
	public void testHighLoad() throws Exception {
		watchService.start();
		List<Path> files = new ArrayList<Path>();
		for(int i = 0; i < 10000; ++i) {
			Path p = Paths.get(basePath.toString(), String.format("%s.txt", i));
			assertTrue(p.toFile().createNewFile());
			files.add(p);
		}
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(files.size())).onLocalFileCreated(anyObject(), false);
		
	}
	
	@Test
	public void testManyFoldersAndEmptyFiles() throws Exception {
		watchService.start();
        Path base = basePath;
        int numFolders = 100;
        int numFilesPerFolder = 1000;
        List<Path> files = new ArrayList<Path>(numFolders*numFilesPerFolder);
		for (int k = 0; k < numFolders; ++k) {
			Path sub = Paths.get(String.format("%s", k));
			Files.createDirectory(base.resolve(sub));
			for (int i = 0; i < numFilesPerFolder; ++i) {
				Path f = sub.resolve(String.format("%s.txt", i));
				Path fullPath = base.resolve(f);
				Files.createFile(fullPath);
				files.add(f);
			}
		}
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(numFolders + numFolders*numFilesPerFolder)).onLocalFileCreated(anyObject(), false);
	}
	
	
	@Test
	public void testManyEmptyFiles() throws Exception {
		watchService.start();
        Path base = basePath;
        int numFolders = 10;
        int numFilesPerFolder = 10000;
        List<Path> files = new ArrayList<Path>(numFolders*numFilesPerFolder);
		for (int k = 0; k < numFolders; ++k) {
			Path sub = Paths.get(String.format("%s", k));
			Files.createDirectory(base.resolve(sub));
			for (int i = 0; i < numFilesPerFolder; ++i) {
				Path f = sub.resolve(String.format("%s.txt", i));
				Path fullPath = base.resolve(f);
				Files.createFile(fullPath);
				files.add(f);
			}
		}
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(numFolders + numFolders*numFilesPerFolder)).onLocalFileCreated(anyObject(), false);
	}
	
	@Test 
	public void testManyFoldersAndSmallFiles() throws Exception {
		watchService.start();
        Path base = basePath;
        int numFolders = 100;
        int numFilesPerFolder = 1000;
        List<Path> files = new ArrayList<Path>(numFolders*numFilesPerFolder);
		for (int k = 0; k < numFolders; ++k) {
			Path sub = Paths.get(String.format("%s", k));
			Files.createDirectory(base.resolve(sub));
			for (int i = 0; i < numFilesPerFolder; ++i) {
				Path f = sub.resolve(String.format("%s.txt", i));
				Path fullPath = base.resolve(f);
				byte[] bytesToWrite = fullPath.toString().getBytes(Charset.forName("UTF-8"));
				Files.write(fullPath, bytesToWrite);
				files.add(f);
			}
		}
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(numFolders + numFolders*numFilesPerFolder)).onLocalFileCreated(anyObject(), false);
	}
	
	@Test
	public void testManySmallFiles() throws Exception {
		watchService.start();
        Path base = basePath;
        int numFolders = 10;
        int numFilesPerFolder = 10000;
		List<Path> files = new ArrayList<Path>(numFolders*numFilesPerFolder);
		for (int k = 0; k < numFolders; ++k) {
			Path sub = Paths.get(String.format("%s", k));
			Files.createDirectory(base.resolve(sub));
			for (int i = 0; i < numFilesPerFolder; ++i) {
				Path f = sub.resolve(String.format("%s.txt", i));
				Path fullPath = base.resolve(f);
				byte[] bytesToWrite = fullPath.toString().getBytes(Charset.forName("UTF-8"));
				Files.write(fullPath, bytesToWrite);
				files.add(f);
			}
		}
		sleep();
		Mockito.verify(fileEventListener, Mockito.times(numFolders + numFolders*numFilesPerFolder)).onLocalFileCreated(anyObject(), false);
	}
	
	private void sleep() throws InterruptedException {
		Thread.sleep(SLEEP_TIME);
	}

}

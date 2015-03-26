package org.peerbox.watchservice;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.integration.TestPeerWaspConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeFolderWatchServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(NativeFolderWatchServiceTest.class);
	private FolderWatchService watchService;
	private FileEventManager eventManager;
	private ActionExecutor actionExecutor;
	private FileTree fileTree;

	@Mock
	private IFileManager fileManager;
	private static Path basePath;

	private static final int NUM_CHARS_SMALL_FILE = 50*1024;
	private static final int NUM_CHARS_BIG_FILE = 50*1024*1024;
//	private static final int SLEEP_TIME = 4000;

	@BeforeClass
	public static void setup() throws Exception {
		basePath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerWasp_FolderWatchServiceTest");
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

		watchService = new FolderWatchService();
		fileTree = new FileTree(basePath, true);
		eventManager = new FileEventManager(fileTree, null);
		actionExecutor = new ActionExecutor(eventManager, fileManager, new TestPeerWaspConfig());
		actionExecutor.setWaitForActionCompletion(false);
		actionExecutor.start();
		watchService.addFileEventListener(eventManager);

		logger.info("Running");
	}

	@After
	public void cleanFolder() throws Exception {
		watchService.stop();
		Thread.sleep(500);
		FileUtils.cleanDirectory(basePath.toFile());
	}

	private void sleep() throws InterruptedException {
		Thread.sleep(actionExecutor.getPeerWaspConfig().getAggregationIntervalInMillis() * 2);
	}


	/**
	 * Create a file and test whether it gets handled by the
	 * handleCreateEvent and eventually send over to H2H.add
	 *
	 * @throws Exception
	 */
	@Test
	public void testFileCreate() throws Exception {
		watchService.start(basePath);

		// create new file
		File add = Paths.get(basePath.toString(), "add_empty.txt").toFile();
		add.createNewFile();
		sleep();

		Mockito.verify(fileManager, Mockito.times(1)).add(add.toPath());
	}

	/**
	 * Create a file with some data in it and test whether
	 * it gets handled by the handleCreateEvent
	 *
	 * @throws Exception
	 */
	@Test
	public void testSmallFileCreate() throws Exception {
		watchService.start(basePath);

		// create new small file
		File add = Paths.get(basePath.toString(), "add_small.txt").toFile();

		FileWriter out = new FileWriter(add);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();

		Mockito.verify(fileManager, Mockito.times(1)).add(add.toPath());
	}

	/**
	 * Create a file which contains a large portion of data and test
	 * whether it gets handled by the handleCreateEvent
	 *
	 * @throws Exception
	 */
	@Test
	public void testBigFileCreate() throws Exception {
		watchService.start(basePath);

		// create new big file
		File add = Paths.get(basePath.toString(), "add_big.txt").toFile();

		FileWriter out = new FileWriter(add);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();
		sleep();

		Mockito.verify(fileManager, Mockito.times(1)).add(add.toPath());
	}

	/**
	 * Test whether a file which already was added to H2H does not
	 * lead to a hard-delete when it is desynchronized.
	 *
	 * @throws Exception
	 */
	@Test
	public void testFileSoftDelete() throws Exception {
		// create new file
		File delete = Paths.get(basePath.toString(), "delete_empty.txt").toFile();
		delete.createNewFile();
		sleep();

		watchService.start(basePath);
		//delete newly created file
		FileUtils.forceDelete(delete);
		sleep();

		Mockito.verifyNoMoreInteractions(fileManager);
	}

	/**
	 * Test whether a modify event will be detected when a file
	 * gets altered
	 *
	 * @throws Exception
	 */
	@Test
	public void testFileModify() throws Exception {
		watchService.start(basePath);

		// create new small file
		File modify = Paths.get(basePath.toString(), "modify_small.txt").toFile();

		FileWriter out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).add(modify.toPath());

		// modify newly created file
		out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();

		Mockito.verify(fileManager, Mockito.times(1)).update(modify.toPath());
	}

	/**
	 * Test whether a modify event of a big file gets detected when a file
	 *  gets altered
	 *
	 * @throws Exception
	 */
	@Test
	public void testBigFileModify() throws Exception {
		watchService.start(basePath);

		// create new small file
		File modify = Paths.get(basePath.toString(), "modify_big.txt").toFile();

		FileWriter out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).add(modify.toPath());

		// modify newly created file
		out = new FileWriter(modify);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();
		sleep();

		Mockito.verify(fileManager).update(modify.toPath());

	}

	/**
	 * Test whether renaming a file gets recognized as an move event
	 * @throws Exception
	 */
	@Test
	public void testFileRename() throws Exception {
		watchService.start(basePath);

		File rename = Paths.get(basePath.toString(), "rename.txt").toFile();
		File newName = Paths.get(basePath.toString(), "rename_rename.txt").toFile();

		FileWriter out = new FileWriter(rename);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();

		Mockito.verify(fileManager, Mockito.times(1)).add(rename.toPath());

		rename.renameTo(newName);
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).move(rename.toPath(), newName.toPath());

	}

	/**
	 * Test whether the movement of a file to an existing sub directory gets recognized
	 * as an move event
	 *
	 * @throws Exception
	 */
	@Test
	public void testFileMove() throws Exception {
		watchService.start(basePath);

		// create new sub directory
		String subDirString = "subDir";
		File subDir = Paths.get(basePath.toString(), subDirString).toFile();
		FileUtils.forceMkdir(subDir);

		// create new file
		File file = Paths.get(basePath.toString(), "move.txt").toFile();
		// target file path
		File newFile = Paths.get(basePath.toString(), subDirString, "move.txt").toFile();

		FileWriter out = new FileWriter(file);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();
		sleep();

		Mockito.verify(fileManager, Mockito.times(1)).add(file.toPath());

		// move file to new target path
		FileUtils.moveFile(file, newFile);
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).move(file.toPath(), newFile.toPath());
	}

	/**
	 * Copy an existing file to another location and check whether
	 * the appropriate event gets triggered
	 *
	 * @throws Exception
	 */
	@Test
	public void testSmallFileCopy() throws Exception {
		File file = Paths.get(basePath.toString(), "original.txt").toFile();
		File newFile = Paths.get(basePath.toString(), "copy.txt").toFile();
		FileWriter out = new FileWriter(file);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
		out.close();

		watchService.start(basePath);
		FileUtils.copyFile(file, newFile);
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).add(newFile.toPath());
		Mockito.verify(fileManager, Mockito.never()).delete(file.toPath());
		Mockito.verify(fileManager, Mockito.never()).move(file.toPath(), newFile.toPath());
	}

	@Test
	public void testBigFileCopy() throws Exception {
		File file = Paths.get(basePath.toString(), "original_big.txt").toFile();
		File newFile = Paths.get(basePath.toString(), "copy_big.txt").toFile();

		FileWriter out = new FileWriter(file);
		WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_BIG_FILE);
		out.close();

		watchService.start(basePath);
		FileUtils.copyFile(file, newFile);
		sleep();
		Mockito.verify(fileManager, Mockito.times(1)).add(newFile.toPath());
		Mockito.verify(fileManager, Mockito.never()).delete(file.toPath());
		Mockito.verify(fileManager, Mockito.never()).move(file.toPath(), newFile.toPath());
	}

	@Test
	public void testManySimultaneousEvents() throws Exception {
		watchService.start(basePath);

		List<File> folderList = new ArrayList<File>();
		List<File> fileList = new ArrayList<File>();

		int numberOfFolders = WatchServiceTestHelpers.randomInt(5, 50);
		int numberOfFiles = WatchServiceTestHelpers.randomInt(200, 1000);

		// create random folders & save files(paths) in list
		String folderName = null;

		for (int i = 1; i <= numberOfFolders; i++){

			folderName = WatchServiceTestHelpers.getRandomString(15, "abcdefghijklmnopqrstuvwxyz123456789");
			String subDirString = "\\" + folderName  +"\\";
			File subDir = Paths.get(basePath.toString(), subDirString).toFile();
			FileUtils.forceMkdir(subDir);

			folderList.add(subDir);
		}

		// create random files in base path & save files(paths) in list
		File randomFile;

		for (int i = 1; i <= numberOfFiles; i++){

			String randomFileName = WatchServiceTestHelpers.getRandomString(15, "abcdefghijklmnopqrstuvwxyz123456789");

			randomFile = Paths.get(basePath.toString(), randomFileName + ".txt").toFile();
			FileWriter out = new FileWriter(randomFile);
			WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
			out.close();

			fileList.add(randomFile);
		}

		// pick a random number of files and move them randomly to folders
		int randomNumberOfMovements = WatchServiceTestHelpers.randomInt(1, numberOfFiles);

		for (int i = 1; i <= randomNumberOfMovements; i++){

			// randomly pick a file from the fileList
			int randomFilePick = WatchServiceTestHelpers.randomInt(0, fileList.size()-1);
			File randomFileToMove = fileList.get(randomFilePick);

			// randomly pick a target sub directory
			int randomFolderPick = WatchServiceTestHelpers.randomInt(0, folderList.size()-1);
			File randomTargetFolder = folderList.get(randomFolderPick);

			FileUtils.moveFileToDirectory(randomFileToMove, randomTargetFolder, false);

			// remove moved file from file list
			fileList.remove(randomFileToMove);
		}

		sleep();

	//	Mockito.verify(fileManager, Mockito.times(randomNumberOfMovements)).move(Matchers.anyObject(), Matchers.anyObject());
	}


	@Test
	public void createManyFiles() throws Exception{
		int numFiles = 1000;
		watchService.start(basePath);
		List<Path> files = new ArrayList<Path>();

		while(numFiles > 0) {
			String randomFileName = WatchServiceTestHelpers.getRandomString(5, "abcdfg1234");
			Path file = Paths.get(basePath.toString(), randomFileName + ".txt");
			FileWriter out = new FileWriter(file.toFile());
			WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
			out.close();
			files.add(file);
			--numFiles;
		}
		sleep();

		Iterator<Path> it = files.iterator();
		while(it.hasNext()) {
			Mockito.verify(fileManager, Mockito.times(1)).add(it.next());
		}
	}


	@Test @Ignore
	public void testCopyFolder() throws Exception {
		// create folder with files
		int numFiles = 100;
		Path original = Paths.get(basePath.toString(), "original_folder");
		Files.createDirectory(original);

		List<Path> files = new ArrayList<Path>();
		Path copy = Paths.get(basePath.toString(), "copy_folder");
		files.add(original);
		for(int i = 0; i < numFiles; ++i) {
			Path f = Paths.get(original.toString(), String.format("file_%s.txt", i));
			FileWriter out = new FileWriter(f.toFile());
			WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
			out.close();
			files.add(Paths.get(copy.toString(), f.getFileName().toString()));

		}

		watchService.start(basePath);

		// copy folder
		FileUtils.copyDirectory(original.toFile(), copy.toFile());
		Thread.sleep(actionExecutor.getPeerWaspConfig().getAggregationIntervalInMillis() * 2);

		Mockito.verify(fileManager, Mockito.times(1)).add(copy);
//		Iterator<Path> it = files.iterator();
//		while(it.hasNext()) {
//			Path newFile = it.next();
////			Path newFile = Paths.get(copy.toString(), oldFile.getFileName().toString());
//			assertTrue(Files.exists(newFile));
//			Object obj = Mockito.when(fileManager.add(newFile)).thenReturn(null);
//
//			Mockito.verify(fileManager, Mockito.times(1)).add(newFile);
//			Mockito.verify(fileManager, Mockito.never()).delete(oldFile);
//			Mockito.verify(fileManager, Mockito.never()).move(oldFile, newFile);
		//}
		// TODO: test not implemented correctly yet
		//fail();


//		final int nrElements = files.size();
		final ArgumentCaptor<Path> captor = ArgumentCaptor.forClass(Path.class);
//
		Mockito.verify(fileManager).add(captor.capture());
//
//		// This is using assertJ
//
//		assertThat(captor.getAllValues()).isEqualTo(myObjects);
	}

	@Test
	public void testCreateManyFiles() throws Exception{
		watchService.start(basePath);
		File file = null;
		int fileNumbers = 1000;

		for (int i = 1; i <= fileNumbers; i++){

			String randomFileName = WatchServiceTestHelpers.getRandomString(9, "abcdefg123456789");

			file = Paths.get(basePath.toString(), randomFileName + ".txt").toFile();
			FileWriter out = new FileWriter(file);
			WatchServiceTestHelpers.writeRandomData(out, NUM_CHARS_SMALL_FILE);
			out.close();
			// System.out.println("File added: Itemnr.: " + i);
		}
		sleep();

		Mockito.verify(fileManager, Mockito.times(fileNumbers)).add(Matchers.anyObject());
	}

}

package org.peerbox.watchservice;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.integration.TestPeerWaspConfig;

@Deprecated
public class FileWalkerTest {

	private static int nrFiles = 3;
	private static String parentPath = System.getProperty("user.home") + File.separator + "PeerBox_FileWalkerTest" + File.separator;
	private static File testDirectory;
	private static ArrayList<String> filePaths = new ArrayList<String>();
	private static ArrayList<File> files = new ArrayList<File>();
	private static FileEventManager manager;
	private static FileTree fileTree;
	
	private TestPeerWaspConfig config = new TestPeerWaspConfig();

	@Mock
	private IFileManager fileManager;

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
		fileTree = new FileTree(Paths.get(parentPath), null, true);
		manager = new FileEventManager(fileTree, null);
	}

	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}

	@AfterClass @Ignore
	public static void rollback(){
		for(int i = 0; i < nrFiles; i++){
			files.get(i).delete();
		}
		assertTrue(testDirectory.delete());
	}

	/**
	 * This test checks if events lost by the FolderWatchService are collected and
	 * executed correctly by the FileWalker. To do that, three files are created, two
	 * of them are announced to the FolderWatchService, and one is omitted (to simulate
	 * the lost event. The FileWalker checks the files and triggers the corresponding event.
	 *
	 * Then, a file delete and a modification are performed, both without a reaction of the
	 * FolderWatchService. The FileWalker then takes care and triggers the events.
	 */
	@Test
	public void indexDirectoryRecursivelyTest(){
		FileWalker walker = new FileWalker(Paths.get(parentPath), manager);

		addTwoOfThreeCreateEvents();

		handleMissingCreateEvents(walker);
		FileEventManagerTest.sleepMillis(config.getAggregationIntervalInMillis() * 2);

		handleUnnoticedDeleteEvent(walker);
		FileEventManagerTest.sleepMillis(config.getAggregationIntervalInMillis() * 2);

		handleUnnoticedModifyEvent(walker);
		FileEventManagerTest.sleepMillis(config.getAggregationIntervalInMillis() * 2);

	}

	private void handleUnnoticedModifyEvent(FileWalker walker) {
		try {
			PrintWriter writer = new PrintWriter(files.get(1).getAbsolutePath(), "UTF-8");
			writer.println("Change the content of the file, this is considered as a modify event");
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(manager.getFileComponentQueue().size() == 0);
		walker.alignActionMaps();
		assertTrue(manager.getFileComponentQueue().size() == 1);


	}

	private void handleUnnoticedDeleteEvent(FileWalker walker) {
		//delete file without notifying the event manager, use file walker to catch the event
		files.get(0).delete();
		assertTrue(manager.getFileComponentQueue().size() == 0);
		walker.alignActionMaps();
		assertTrue(manager.getFileComponentQueue().size() == 1);

	}

	private void handleMissingCreateEvents(FileWalker walker) {
		//catch third create using the file walker
		walker.alignActionMaps();
		assertTrue(manager.getFileComponentQueue().size() == 1);
		//assertTrue(manager.getFilePathToAction().size() == 3);
	}

	private void addTwoOfThreeCreateEvents() {
		//inform event manager about two creates, wait until they're handled
		manager.onLocalFileCreated(Paths.get(filePaths.get(0)));
		manager.onLocalFileCreated(Paths.get(filePaths.get(1)));
		FileEventManagerTest.sleepMillis(config.getAggregationIntervalInMillis() * 2);
		assertTrue(manager.getFileComponentQueue().size() == 0);
		//assertTrue(manager.getFilePathToAction().size() == 2);
	}
}

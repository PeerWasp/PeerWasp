package org.peerbox.watchservice.filetree;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FileLeaf;
import org.peerbox.watchservice.FolderComposite;

public class FolderCompositeTest {

	private static String parentPath = System.getProperty("user.home") + File.separator + "PeerBox_FolderCompositeTest" + File.separator; 
	private static File testDirectory;

	private static String fileOnRootStr = parentPath + "file.txt";
	private static String dirOnRootStr = parentPath + "dir";
	private static String dirInDirOnRootStr = parentPath + "dir" + File.separator + "dir2";
	private static String fileInNewDirStr = parentPath  + "dir" + File.separator + "file.txt";
	private static String fileInDirInDirOnRootStr = dirInDirOnRootStr + File.separator + "file.txt";
	
	private static File fileOnRoot;
	private static File dirInDirOnRoot;
	private static File fileInNewDir;
	private static File fileInDirInDirOnRoot;
	private static File dirOnRoot;
	
	private DummyFileEventManager fileEventManager = new DummyFileEventManager();
	
	@BeforeClass
	public static void setup(){
		testDirectory = new File(parentPath);
		testDirectory.mkdir();
		try {
				
			fileOnRoot = new File(fileOnRootStr);
			dirInDirOnRoot = new File(dirInDirOnRootStr);
			dirOnRoot = new File(dirOnRootStr);
			fileInNewDir = new File(fileInNewDirStr);
			fileInDirInDirOnRoot = new File(fileInDirInDirOnRootStr);
			
			fileOnRoot.createNewFile();
			dirInDirOnRoot.mkdirs();
			dirOnRoot.mkdirs();
			fileInNewDir.createNewFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void rollBack(){
		fileInDirInDirOnRoot.delete();
		dirInDirOnRoot.delete();
		fileOnRoot.delete();
		fileInNewDir.delete();
		dirOnRoot.delete();

		testDirectory.delete();
		
	}
	
	/**
	 * This test checks if putComponent calls work by issuing getComponent calls on
	 * the added components. Furthermore, delete and updates are tested,
	 * 
	 * - add a file to the root directory
	 * - add a file to a sub directory (such that the sub dir is created as well)
	 * - add a directory
	 */
	
	@Test
	public void fileTreeOperationsTest(){

		FolderComposite fileTree = new FolderComposite(Paths.get(parentPath), true);
		FileLeaf fileOnRoot = new FileLeaf(Paths.get(fileOnRootStr));
		FileLeaf fileInNewDir = new FileLeaf(Paths.get(fileInNewDirStr));
		fileInNewDir.getAction().setFile(fileInNewDir);
		fileOnRoot.getAction().setFile(fileOnRoot);
		fileTree.putComponent(fileOnRootStr, fileOnRoot);
		fileTree.putComponent(fileInNewDirStr, fileInNewDir);
		
		FileComponent component = fileTree.getComponent(fileOnRootStr);
		assertTrue(component instanceof FileLeaf);
		assertTrue(component.getAction().getFilePath().toString().equals(fileOnRootStr));
		
		//check if it is possible to add a file into a new directory
		component = fileTree.getComponent(fileInNewDirStr);
		assertTrue(component instanceof FileLeaf);
		assertTrue(component.getPath().toString().equals(fileInNewDirStr));
		
		component = fileTree.getComponent(dirOnRootStr);
		assertTrue(component instanceof FolderComposite);
		assertTrue(component.getPath().toString().equals(dirOnRootStr));
		
		component = fileTree.getComponent(dirInDirOnRootStr);
		assertNull(component);

		fileTree.putComponent(dirInDirOnRootStr, new FolderComposite(Paths.get(dirInDirOnRootStr), true));
		component = fileTree.getComponent(dirInDirOnRootStr);
		assertTrue(component instanceof FolderComposite);
		assertTrue(component.getPath().toString().equals(dirInDirOnRootStr));
		
		bubbleContentHashUpdateTest(fileTree);
		deleteComponentTest(fileTree);	
	}
	
	private void bubbleContentHashUpdateTest(FolderComposite fileTree){
		//put a new file in a lower directory
		FileLeaf fileInDirInDirOnRoot = new FileLeaf(Paths.get(fileInDirInDirOnRootStr));
		fileInDirInDirOnRoot.getAction().setFile(fileInDirInDirOnRoot);
		fileTree.putComponent(fileInDirInDirOnRootStr, fileInDirInDirOnRoot);
		FileComponent component = fileTree.getComponent(fileInDirInDirOnRootStr);
		assertTrue(component instanceof FileLeaf);
		assertTrue(component.getPath().toString().equals(fileInDirInDirOnRootStr));
		
		//get old hash of root and file, then modify the file
		String oldHashRoot = fileTree.getContentHash();
		String oldHashFile = component.getContentHash();
		
		try {
			PrintWriter writer = new PrintWriter(fileInDirInDirOnRootStr, "UTF-8");
			writer.println("HelloWorld");
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		fileInDirInDirOnRoot.bubbleContentHashUpdate();
		String newHashRoot = fileTree.getContentHash();
		String newHashFile = component.getContentHash();

		assertFalse(newHashRoot.equals(oldHashRoot));
		assertFalse(newHashFile.equals(oldHashFile));

	}
	
	private void deleteComponentTest(FolderComposite fileTree){
		//ensure the file exists
		FileComponent component = fileTree.deleteComponent(fileOnRootStr);
		assertTrue(component instanceof FileLeaf);
		//ensure it cannot be received anymore
		component = fileTree.getComponent(fileOnRootStr);
		assertNull(component);
		//ensure new delete returns null
		component = fileTree.deleteComponent(fileOnRootStr);
		assertNull(component);
		
		//ensure file in sub directory still exists
		component = fileTree.getComponent(fileInNewDirStr);
		assertTrue(component instanceof FileLeaf);
		
		String oldContentNamesHashRoot = fileTree.getContentNamesHash();
		//delete subdirectory, ensure contained files and directories are deleted as well
		component = fileTree.deleteComponent(dirOnRootStr);
		assertFalse(fileTree.getContentNamesHash().equals(oldContentNamesHashRoot));
		oldContentNamesHashRoot = fileTree.getContentNamesHash();
		assertTrue(component instanceof FolderComposite);
		
		
		component = fileTree.deleteComponent(dirInDirOnRootStr);
		assertNull(component);
		assertTrue(fileTree.getContentNamesHash().equals(oldContentNamesHashRoot));
		component = fileTree.deleteComponent(fileInNewDirStr);
		assertNull(component);
		assertTrue(fileTree.getContentNamesHash().equals(oldContentNamesHashRoot));
	}
	
	
	/**
	 * This test ensures that create/delete events of components trigger hierarchical updates of
	 * the contentNamesHash value, which encodes the filenames of all recusively contained components
	 */

	public void bubbleContentNamesHashUpdateTest(){
		
	}
	
	
}

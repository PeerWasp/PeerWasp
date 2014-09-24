package org.peerbox.watchservice.filetree;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FileLeaf;
import org.peerbox.watchservice.FolderComposite;
import org.peerbox.watchservice.SerializeService;

public class TreePersistence {
	
	private static String parentPath = System.getProperty("user.home") + File.separator + "PeerBox_FolderCompositeTest" + File.separator; 
	private static File testDirectory;
	String objectFile = parentPath + "FileTree.ser";

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
	
	//@AfterClass
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
		fileTree.putComponent(fileOnRootStr, new FileLeaf(Paths.get(fileOnRootStr)));
		fileTree.putComponent(fileInNewDirStr, new FileLeaf(Paths.get(fileInNewDirStr)));
		
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
		
		try {
			SerializeService.serialize(fileTree, objectFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		

}

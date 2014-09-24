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

public class TreePersistenceTest {
	
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
	 * This test checks if a tree which represents the file system can be saved as
	 * a XML representation and to read from that XML file in order to create an
	 * identical tree object
	 * 
	 * - add a file to the root directory
	 * - add a file to a sub directory (such that the sub dir is created as well)
	 * - add a directory
	 * - write tree to xml file
	 * - create new tree
	 * - read from xml file
	 * 
	 */
	
	@Test
	public void fileTreeSerializeTest(){

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
		
		
		// write fileTree to XML
		try {
			SerializeService.serializeToXml(fileTree);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		// read from XML file
		FileComponent reloadeadFileTree = SerializeService.deserializeFromXml();
		
		FileComponent newComponent = reloadeadFileTree.getComponent(fileOnRootStr);
		assertTrue(newComponent instanceof FileLeaf);
		assertTrue(newComponent.getAction().getFilePath().toString().equals(fileOnRootStr));
		
		//check if it is possible to add a file into a new directory
		newComponent = reloadeadFileTree.getComponent(fileInNewDirStr);
		assertTrue(newComponent instanceof FileLeaf);
		assertTrue(newComponent.getPath().toString().equals(fileInNewDirStr));
		
		newComponent = reloadeadFileTree.getComponent(dirOnRootStr);
		assertTrue(newComponent instanceof FolderComposite);
		assertTrue(newComponent.getPath().toString().equals(dirOnRootStr));
		
		newComponent = reloadeadFileTree.getComponent(dirInDirOnRootStr);
		System.out.println(newComponent);
	
		assertTrue(newComponent instanceof FolderComposite);
		assertTrue(newComponent.getPath().toString().equals(dirInDirOnRootStr));
	}
	


}

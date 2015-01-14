package org.peerbox.watchservice.filetree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

public class FileComponentTest {

	private static Path parentPath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerBox_FolderCompositeTest");

	private static Path fileOnRootPath = parentPath.resolve("file.txt");
	private static Path dirOnRootPath = parentPath.resolve("dir");
	private static Path dirInDirOnRootPath = parentPath.resolve("dir").resolve("dir2");
	private static Path fileInNewDirPath = parentPath.resolve("dir").resolve("file.txt");
	private static Path fileInDirInDirOnRootPath = dirInDirOnRootPath.resolve("file.txt");

	@BeforeClass
	public static void setup() throws IOException{
		Files.createDirectory(parentPath);

		Files.createFile(fileOnRootPath);
		Files.createDirectories(dirInDirOnRootPath);
		Files.createDirectories(dirOnRootPath);
		Files.createFile(fileInNewDirPath);
	}

	@AfterClass
	public static void rollBack() throws IOException{
		FileUtils.deleteDirectory(parentPath.toFile());
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

		FolderComposite fileTree = new FolderComposite(parentPath, true);
		FileLeaf fileOnRoot = new FileLeaf(fileOnRootPath, true);
		FileLeaf fileInNewDir = new FileLeaf(fileInNewDirPath, true);
		fileInNewDir.getAction().setFile(fileInNewDir);
		fileOnRoot.getAction().setFile(fileOnRoot);
		fileTree.putComponent(fileOnRootPath, fileOnRoot);
		fileTree.putComponent(fileInNewDirPath, fileInNewDir);

		FileComponent component = fileTree.getComponent(fileOnRootPath);
		assertTrue(component instanceof FileLeaf);
		assertTrue(component.getAction().getFile().getPath().equals(fileOnRootPath));

		//check if it is possible to add a file into a new directory
		component = fileTree.getComponent(fileInNewDirPath);
		assertTrue(component instanceof FileLeaf);
		assertTrue(component.getPath().equals(fileInNewDirPath));

		component = fileTree.getComponent(dirOnRootPath);
		assertTrue(component instanceof FolderComposite);
		assertTrue(component.getPath().equals(dirOnRootPath));

		component = fileTree.getComponent(dirInDirOnRootPath);
		assertNull(component);

		fileTree.putComponent(dirInDirOnRootPath, new FolderComposite(dirInDirOnRootPath, true));
		component = fileTree.getComponent(dirInDirOnRootPath);
		assertTrue(component instanceof FolderComposite);
		assertTrue(component.getPath().equals(dirInDirOnRootPath));

		bubbleContentHashUpdateTest(fileTree);
		deleteComponentTest(fileTree);
	}

	private void bubbleContentHashUpdateTest(FolderComposite fileTree){
		//put a new file in a lower directory
		FileLeaf fileInDirInDirOnRoot = new FileLeaf(fileInDirInDirOnRootPath, true);
		fileInDirInDirOnRoot.getAction().setFile(fileInDirInDirOnRoot);
		fileTree.putComponent(fileInDirInDirOnRootPath, fileInDirInDirOnRoot);
		FileComponent component = fileTree.getComponent(fileInDirInDirOnRootPath);
		assertTrue(component instanceof FileLeaf);
		assertTrue(component.getPath().equals(fileInDirInDirOnRootPath));

		//get old hash of root and file, then modify the file
		String oldHashRoot = fileTree.getContentHash();
		String oldHashFile = component.getContentHash();

		try {
			PrintWriter writer = new PrintWriter(fileInDirInDirOnRootPath.toFile(), "UTF-8");
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
		FileComponent component = fileTree.deleteComponent(fileOnRootPath);
		assertTrue(component instanceof FileLeaf);
		//ensure it cannot be received anymore
		component = fileTree.getComponent(fileOnRootPath);
		assertNull(component);
		//ensure new delete returns null
		component = fileTree.deleteComponent(fileOnRootPath);
		assertNull(component);

		//ensure file in sub directory still exists
		component = fileTree.getComponent(fileInNewDirPath);
		assertTrue(component instanceof FileLeaf);

		String oldContentNamesHashRoot = fileTree.getStructureHash();
		//delete subdirectory, ensure contained files and directories are deleted as well
		component = fileTree.deleteComponent(dirOnRootPath);
		assertFalse(fileTree.getStructureHash().equals(oldContentNamesHashRoot));
		oldContentNamesHashRoot = fileTree.getStructureHash();
		assertTrue(component instanceof FolderComposite);


		component = fileTree.deleteComponent(dirInDirOnRootPath);
		assertNull(component);
		assertTrue(fileTree.getStructureHash().equals(oldContentNamesHashRoot));
		component = fileTree.deleteComponent(fileInNewDirPath);
		assertNull(component);
		assertTrue(fileTree.getStructureHash().equals(oldContentNamesHashRoot));
	}


	/**
	 * This test ensures that create/delete events of components trigger hierarchical updates of
	 * the contentNamesHash value, which encodes the filenames of all recusively contained components
	 */

	public void bubbleContentNamesHashUpdateTest(){

	}


}

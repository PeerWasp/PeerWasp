package org.peerbox.app.manager.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.events.implementations.FileAddEvent;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.security.HashUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.peerbox.presenter.settings.synchronization.PathItem;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

public class FileInfoTest {

	@Test
	public void testFileInfo_PathBoolean() {
		Path file = Paths.get("/path/to/a/file.txt");
		FileInfo fileInfo = new FileInfo(file, false);
		assertTrue(fileInfo.isFile());
		assertEquals(file, fileInfo.getPath());
		assertEquals("", fileInfo.getContentHash());

		Path folder = Paths.get("/path/to/a/folder");
		FileInfo folderInfo = new FileInfo(folder, true);
		assertTrue(folderInfo.isFolder());
		assertEquals(folder, folderInfo.getPath());
		assertEquals("", folderInfo.getContentHash());
	}

	@Test
	public void testFileInfo_PathBooleanString() {
		Path file = Paths.get("/path/to/a/file.txt");
		String fileHash = "hash1";
		FileInfo fileInfo = new FileInfo(file, false, fileHash);
		assertTrue(fileInfo.isFile());
		assertEquals(file, fileInfo.getPath());
		assertEquals(fileHash, fileInfo.getContentHash());

		Path folder = Paths.get("/path/to/a/folder");
		String folderHash = "hash2";
		FileInfo folderInfo = new FileInfo(folder, true, folderHash);
		assertTrue(folderInfo.isFolder());
		assertEquals(folder, folderInfo.getPath());
		assertEquals(folderHash, folderInfo.getContentHash());
	}

	@Test
	public void testFileInfo_FileComponent() {
		// file
		Path file = Paths.get("/path/to/a/file.txt");
		FileComponent fileComponent = new FileLeaf(file, true);
		fileComponent.setContentHash("hash1");
		FileInfo fileInfo = new FileInfo(fileComponent);
		assertEquals(file, fileInfo.getPath());
		assertTrue(fileInfo.isFile());
		assertEquals("hash1", fileInfo.getContentHash());

		// folder
		Path folder = Paths.get("/path/to/a/folder");
		FileComponent folderComponent = new FolderComposite(folder, true);
		FileInfo folderInfo = new FileInfo(folderComponent);
		assertEquals(folder, folderInfo.getPath());
		assertTrue(folderInfo.isFolder());
		assertEquals("1B2M2Y8AsgTpgAmY7PhCfg==", folderInfo.getContentHash()); // empty
	}

	@Test
	public void testFileInfo_FileNode() {
		// file
		Path file = Paths.get("/path/to/a/file.txt");
		FileNode fileNode = new FileNode(null, file.toFile(),
				file.getFileName().toString(), HashUtil.hash("hello world".getBytes()), new HashSet<>());
		FileInfo fileInfo = new FileInfo(fileNode);
		assertEquals(file, fileInfo.getPath());
		assertTrue(fileInfo.isFile());
		assertEquals("XrY7u+Ae7tCTyyK7j1rNww==", fileInfo.getContentHash());

		// folder
		Path folder = Paths.get("/path/to/a/folder");
		FileNode folderNode = new FileNode(null, folder.toFile(),
				folder.getFileName().toString(), null, new HashSet<>());
		FileInfo folderInfo = new FileInfo(folderNode);
		assertEquals(folder, folderInfo.getPath());
		assertTrue(folderInfo.isFolder());
		assertEquals("", folderInfo.getContentHash());

	}

	@Test
	public void testFileInfo_IFileEvent() {
		// file
		File file = new File("/path/to/a/file.txt");
		IFileEvent fileEvent = new FileAddEvent(file, true);
		FileInfo fileInfo = new FileInfo(fileEvent);
		assertEquals(file.toPath(), fileInfo.getPath());
		assertTrue(fileInfo.isFile());
		assertEquals("", fileInfo.getContentHash()); // file event does not have conent hash

		// folder
		File folder = new File("/path/to/a/folder");
		IFileEvent folderEvent = new FileAddEvent(folder, false);
		FileInfo folderInfo = new FileInfo(folderEvent);
		assertEquals(folder.toPath(), folderInfo.getPath());
		assertTrue(folderInfo.isFolder());
		assertEquals("", folderInfo.getContentHash()); // file event does not have conent hash
	}

	@Test @Ignore // requires initialization of javafx
	public void testFileInfo_PathItem() {
		Path file = Paths.get("/path/to/a/file.txt");
		PathItem fileItem = new PathItem(file, true, new HashSet<>());
		FileInfo fileInfo = new FileInfo(fileItem);
		assertEquals(fileItem.getPath(), fileInfo.getPath());
		assertTrue(fileInfo.isFile());
		assertEquals("", fileInfo.getContentHash()); // path item does not have conent hash


		Path folder = Paths.get("/path/to/a/folder");
		PathItem folderItem = new PathItem(folder, false, new HashSet<>());
		FileInfo folderInfo = new FileInfo(folderItem);
		assertEquals(fileItem.getPath(), folderInfo.getPath());
		assertTrue(folderInfo.isFolder());
		assertEquals("", folderInfo.getContentHash()); // path item does not have conent hash
	}

	@Test
	public void testSetAndGetContentHash() {
		Path file = Paths.get("/path/to/a/file.txt");
		FileInfo fInfo = new FileInfo(file, true);

		fInfo.setContentHash("newHash_1");
		assertEquals("newHash_1", fInfo.getContentHash());
		fInfo.setContentHash("newHash_2");
		assertEquals("newHash_2", fInfo.getContentHash());
	}

	@Test
	public void testIsFileOrFolder() {
		// file
		Path file = Paths.get("/path/to/a/file.txt");
		FileInfo fileInfo = new FileInfo(file, false);
		assertFalse(fileInfo.isFolder());
		assertTrue(fileInfo.isFile());

		// folder
		Path folder = Paths.get("/path/to/a/folder");
		FileInfo folderInfo = new FileInfo(folder, true);
		assertTrue(folderInfo.isFolder());
		assertFalse(folderInfo.isFile());
	}

	@Test
	public void testCompareTo() {
		Path a = Paths.get("/path/to/a");
		FileInfo aInfo = new FileInfo(a, false);

		Path b = Paths.get("/path/to/b");
		FileInfo bInfo = new FileInfo(b, false);

		assertEquals(-1, aInfo.compareTo(bInfo));
		assertEquals(0, aInfo.compareTo(aInfo));
		assertEquals(0, bInfo.compareTo(bInfo));
		assertEquals(1, bInfo.compareTo(aInfo));
	}

}

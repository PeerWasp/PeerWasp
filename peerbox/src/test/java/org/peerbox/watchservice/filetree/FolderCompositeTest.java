package org.peerbox.watchservice.filetree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hive2hive.core.security.HashUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.watchservice.PathUtils;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

public class FolderCompositeTest {

	private static Path basePath;

	private Path rootPath;
	private FolderComposite rootFolder;

	private static final boolean cleanupFolder = true;

	private static final String EMPTY_FOLDER_HASH = "1B2M2Y8AsgTpgAmY7PhCfg==";

	@BeforeClass
	public static void beforeClass() throws Exception {
		basePath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerWasp_Test_Tree");
		createDirectoryIfNotExists(basePath);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (cleanupFolder) {
			FileUtils.deleteDirectory(basePath.toFile());
		}
	}

	@Before
	public void beforeTest() throws Exception {
		rootPath = basePath.resolve("root");
		createDirectoryIfNotExists(rootPath);
		rootFolder = new TestFolderComposite(rootPath, true, true);
	}

	@After
	public void afterTest() throws IOException {
		FileUtils.cleanDirectory(basePath.toFile());
	}

	private static void createDirectoryIfNotExists(Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
	}

	private FileComponent createFile(final Path path, final boolean useTestClasses) throws IOException {
		Files.createDirectories(path.getParent());
		Files.createFile(path);
		String content = RandomStringUtils.randomAlphanumeric(1000);
		Files.write(path, content.getBytes());

		if(useTestClasses){
			return new TestFileLeaf(path, true);
		} else {
			return new FileLeaf(path, true);
		}
	}

	private FileComponent createFolder(final Path path, final boolean useTestClasses) throws IOException {
		Files.createDirectories(path);
		if(useTestClasses){
			return new TestFolderComposite(path, true);
		} else {
			return new FolderComposite(path, true);
		}
	}

	private FileComponent createFolder(final Path path) throws IOException {
		return createFolder(path, true);
	}

	private FileComponent createFile(final Path path) throws IOException {
		return createFile(path, true);
	}

	private String computeHashOfString(String content) {
		byte[] rawHash = HashUtil.hash(content.getBytes());
		return PathUtils.base64Encode(rawHash);
	}

	@Test
	public void testNewRoot() {
		// root after creating object
		FolderComposite r = rootFolder = new FolderComposite(rootPath, true, true);
		assertNotNull(r.getAction());
		assertTrue(r.isUploaded());
		assertEquals(rootPath, r.getPath());
		assertNull(r.getParent());
		r.updateContentHash();
		assertEquals(r.getContentHash(), EMPTY_FOLDER_HASH);
		assertTrue(r.isSynchronized());
		assertTrue(r.isFolder());
		assertEquals(r.getStructureHash(), EMPTY_FOLDER_HASH);
		assertTrue(r.getChildren().isEmpty());
		assertTrue(r.isReady());
	}

	@Test
	public void testNewNotRoot() {
		// root after creating object
		FolderComposite r = rootFolder = new FolderComposite(rootPath, true, false);
		assertNotNull(r.getAction());
		assertFalse(r.isUploaded()); // now false
		assertEquals(rootPath, r.getPath());
		assertNull(r.getParent());
		r.updateContentHash();
		assertEquals(r.getContentHash(), EMPTY_FOLDER_HASH);
		assertFalse(r.isSynchronized()); // now false
		assertTrue(r.isFolder());
		assertEquals(r.getStructureHash(), EMPTY_FOLDER_HASH);
		assertTrue(r.getChildren().isEmpty());
	}

	@Test
	public void testPutFileInRoot() throws IOException {
		Path file = rootPath.resolve("file.txt");
		FileComponent f = createFile(file);
		rootFolder.putComponent(file, f);

		// check file
		assertEquals(f.getParent(), rootFolder);
		assertEquals(f.getPath(), file);

		// check children
		assertTrue(rootFolder.getChildren().size() == 1);
		assertTrue(rootFolder.getChildren().containsKey(file.getFileName()));
		assertEquals(rootFolder.getChildren().get(file.getFileName()), f);

		// check content hash
		String contentHash = computeHashOfString(f.getContentHash());
		assertEquals(rootFolder.getContentHash(), contentHash);

		// check name hash
		String structureHash = computeHashOfString(f.getPath().getFileName().toString());
		assertEquals(rootFolder.getStructureHash(), structureHash);
	}

	@Test
	public void testPutFolderInRoot() throws IOException {
		Path folder = rootPath.resolve("folder");
		FileComponent f = createFolder(folder);
		rootFolder.putComponent(folder, f);

		// check folder
		assertEquals(f.getParent(), rootFolder);
		assertEquals(f.getPath(), folder);

		// check children
		assertTrue(rootFolder.getChildren().size() == 1);
		assertTrue(rootFolder.getChildren().containsKey(folder.getFileName()));
		assertEquals(rootFolder.getChildren().get(folder.getFileName()), f);

		// check content hash
		String contentHash = computeHashOfString(f.getContentHash());
		assertEquals(rootFolder.getContentHash(), contentHash);

		// check name hash
		String structureHash = computeHashOfString(f.getPath().getFileName().toString());
		assertEquals(rootFolder.getStructureHash(), structureHash);
	}

	@Test
	public void testPutFileInSubfolder() throws IOException {
		Path fileSub = Paths.get("1", "2", "3", "file.txt");
		Path file = rootPath.resolve(fileSub);
		FileComponent f = createFile(file);
		rootFolder.putComponent(file, f);

		// check file
		FileComponent parent = rootFolder.getComponent(fileSub.getParent());
		assertEquals(parent, f.getParent());
		assertEquals(f.getPath(), file);


		// check ancestors -- for each element of the path:
		FileComponent previous = rootFolder;
		FileComponent current = null;
		for (int i = 1; i < fileSub.getNameCount(); ++i) {
			Path sub = rootPath.resolve(fileSub.subpath(0, i));
			// check parent
			current = rootFolder.getComponent(sub);
			assertNotNull(current);
			assertEquals(previous, current.getParent());
			assertEquals(sub, current.getPath());

			// check children
			assertTrue(previous.isFolder());
			FolderComposite folder = (FolderComposite) previous;
			assertTrue(folder.getChildren().size() == 1);
			assertTrue(folder.getChildren().containsKey(sub.getFileName()));
			assertEquals(folder.getChildren().get(sub.getFileName()), current);

			// check content hash -- parent includes hash of child
			String contentHash = computeHashOfString(current.getContentHash());
			assertEquals(previous.getContentHash(), contentHash);

			// check name hash
			String structureHash = computeHashOfString(current.getPath().getFileName().toString());
			assertEquals(((FolderComposite)previous).getStructureHash(), structureHash);

			previous = current;
		}
	}

	@Test
	public void testPutSubfolders() throws IOException {
		// TODO
	}

	@Test
	public void testGetRoot() throws IOException {
		FileComponent f = rootFolder.getComponent(rootPath);
		assertEquals(f, rootFolder);
	}

	@Test
	public void testGetFileInRoot() throws IOException {
		Path file = rootPath.resolve("aj489tg.txt");
		FileComponent f = createFile(file);
		rootFolder.putComponent(file, f);

		FileComponent get = rootFolder.getComponent(file);
		assertEquals(get, f);
	}

	@Test
	public void testGetFolderInRoot() throws IOException {
		Path folder = rootPath.resolve("o7fhgU");
		FileComponent f = createFolder(folder);
		rootFolder.putComponent(folder, f);

		FileComponent get = rootFolder.getComponent(folder);
		assertEquals(get, f);
	}

	@Test
	public void testGetSubfolders() throws IOException {
		Path folderA = rootPath.resolve("g8uqe");
		Path folderB = folderA.resolve("gzgzt");
		FileComponent fA = createFolder(folderA);
		FileComponent fB = createFolder(folderA);
		rootFolder.putComponent(folderA, fA);
		rootFolder.putComponent(folderB, fB);

		FileComponent get = rootFolder.getComponent(folderA);
		assertEquals(get, fA);
		get = rootFolder.getComponent(folderB);
		assertEquals(get, fB);
	}

	@Test
	public void testGetFileInSubfolder() throws IOException {
		Path folder = rootPath.resolve("g8uqe");
		Path file = folder.resolve("hello.txt");
		FileComponent cFolder = createFolder(folder);
		FileComponent cFile = createFile(file);
		rootFolder.putComponent(folder, cFolder);
		rootFolder.putComponent(file, cFile);

		FileComponent get = rootFolder.getComponent(folder);
		assertEquals(get, cFolder);
		get = rootFolder.getComponent(file);
		assertEquals(get, cFile);
	}

	@Test
	public void testGetNotExising() throws IOException {
		Path existsP = rootPath.resolve("a");
		FileComponent existsC = createFolder(existsP);
		rootFolder.putComponent(existsP, existsC);
		Path notExitsP = existsP.resolve("b");
		FileComponent notExistsC = rootFolder.getComponent(notExitsP);
		assertNull(notExistsC);
	}

	@Test
	public void testGetNotExising_File() throws IOException {
		Path existsP = rootPath.resolve("a");
		FileComponent existsC = createFile(existsP);
		rootFolder.putComponent(existsP, existsC);
		Path notExitsP = existsP.resolve("b");
		FileComponent notExistsC = rootFolder.getComponent(notExitsP);
		assertNull(notExistsC);
	}

	@Test
	public void testDeleteFileInRoot() throws IOException {
		// TODO
	}

	@Test
	public void testDeleteFolderInRoot() throws IOException {
		// TODO
	}

	@Test
	public void testDeleteFileInSubfolder() throws IOException {
		// TODO
	}

	@Test
	public void testDeleteFolderInSubfolder() throws IOException {
		// TODO
	}

	@Test
	public void testUpdateFileHash() throws IOException {
		// TODO
	}

	@Test
	public void testIsActionUploaded() throws IOException {
		Path folderA = rootPath.resolve("fA");
		FileComponent fA = createFolder(folderA);
		rootFolder.putComponent(folderA, fA);

		Path folderB = folderA.resolve("fB");
		FileComponent fB = createFolder(folderB);
		rootFolder.putComponent(folderB, fB);

		Path file = folderB.resolve("test.txt");
		FileComponent fC = createFile(file);
		rootFolder.putComponent(file, fC);

		assertTrue(rootFolder.isUploaded());
		assertFalse(fA.isUploaded());
		assertFalse(fB.isUploaded());
		assertFalse(fC.isUploaded());

		// switch true/false fB
		fB.setIsUploaded(true);
		assertTrue(rootFolder.isUploaded());
		assertFalse(fA.isUploaded());
		assertTrue(fB.isUploaded());
		assertFalse(fC.isUploaded());

		fB.setIsUploaded(false);
		assertTrue(rootFolder.isUploaded());
		assertFalse(fA.isUploaded());
		assertFalse(fB.isUploaded());
		assertFalse(fC.isUploaded());

		// switch true/false fA
		fA.setIsUploaded(true);
		assertTrue(rootFolder.isUploaded());
		assertTrue(fA.isUploaded());
		assertFalse(fB.isUploaded());
		assertFalse(fC.isUploaded());

		fA.setIsUploaded(false);
		assertTrue(rootFolder.isUploaded());
		assertFalse(fA.isUploaded());
		assertFalse(fB.isUploaded());
		assertFalse(fC.isUploaded());
	}

	@Test
	public void testIsSynchronized() throws IOException {
		Path folderA = rootPath.resolve("fA");
		FileComponent fA = createFolder(folderA, false);
		rootFolder.putComponent(folderA, fA);

		Path folderB = folderA.resolve("fB");
		FileComponent fB = createFolder(folderB, false);
		rootFolder.putComponent(folderB, fB);

		Path file = folderB.resolve("test.txt");
		FileComponent fC = createFile(file, false);
		rootFolder.putComponent(file, fC);

		assertTrue(rootFolder.isSynchronized());
		assertFalse(fA.isSynchronized());
		assertFalse(fB.isSynchronized());
		assertFalse(fC.isSynchronized());

		// switch true/false fB
		fB.setIsSynchronized(true);
		assertTrue(rootFolder.isSynchronized());
		assertFalse(fA.isSynchronized());
		assertTrue(fB.isSynchronized());
		assertFalse(fC.isSynchronized());

		fB.setIsSynchronized(false);
		assertTrue(rootFolder.isSynchronized());
		assertFalse(fA.isSynchronized());
		assertFalse(fB.isSynchronized());
		assertFalse(fC.isSynchronized());

		// switch true/false fA
		fA.setIsSynchronized(true);
		assertTrue(rootFolder.isSynchronized());
		assertTrue(fA.isSynchronized());
		assertFalse(fB.isSynchronized());
		assertFalse(fC.isSynchronized());

		fA.setIsSynchronized(false);
		assertTrue(rootFolder.isSynchronized());
		assertFalse(fA.isSynchronized());
		assertFalse(fB.isSynchronized());
		assertFalse(fC.isSynchronized());
	}

	@Test
	public void testIsReady() throws IOException {
		assertTrue(rootFolder.isReady());

		Path folderA = rootPath.resolve("fA");
		FileComponent fA = createFolder(folderA);
		rootFolder.putComponent(folderA, fA);

		Path folderB = folderA.resolve("fB");
		FileComponent fB = createFolder(folderB);
		rootFolder.putComponent(folderB, fB);

		// initial setting
		assertTrue(fA.isReady());
		assertFalse(fB.isReady());

		// switch true/false
		fA.setIsUploaded(true);
		assertTrue(fA.isReady());
		assertTrue(fB.isReady());

		fA.setIsUploaded(false);
		assertTrue(fA.isReady());
		assertFalse(fB.isReady());
	}

	@Test
	public void testPropagateIsUploaded() throws IOException {
		// TODO
	}
}

package org.peerbox.app.manager.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.manager.user.FileAgent;

public class FileAgentTest extends BaseJUnitTest {

	private Path base;
	private Path root;
	private Path cache;

	private FileAgent fileAgent;

	@Before
	public void before() {
		base = Paths.get(FileUtils.getTempDirectoryPath(), "PeerBox_Test");
		root = base.resolve("root");
		cache = base.resolve("cache");

		fileAgent = new FileAgent(root, cache);
	}

	@After
	public void after() throws IOException {
		FileUtils.deleteDirectory(base.toFile());
	}

	@Test
	public void testCacheFolderNotExists() throws IOException {
		assertFalse(Files.exists(cache));

		String data = RandomStringUtils.random(1000);
		fileAgent.writeCache("testKey", data.getBytes());

		assertTrue(Files.exists(cache));
		assertTrue(Files.exists(cache.resolve("testKey")));
	}

	@Test
	public void testWriteRead() throws IOException {
		String data = RandomStringUtils.random(1000);
		fileAgent.writeCache("testWrite", data.getBytes());

		Path f = cache.resolve("testWrite");
		assertTrue(Files.exists(f));

		byte[] content = Files.readAllBytes(f);
		assertNotNull(content);
		assertEquals(data, new String(content));

		byte[] read = fileAgent.readCache("testWrite");
		assertNotNull(read);
		assertEquals(data, new String(read));
	}

	@Test
	public void testWriteReadNull() throws IOException {
		fileAgent.writeCache("testWriteNull", null);

		Path f = cache.resolve("testWriteNull");
		assertFalse(Files.exists(f));

		byte[] read = fileAgent.readCache("testWriteNull");
		assertNull(read);
	}

	@Test
	public void testWriteReadEmpty() throws IOException {
		fileAgent.writeCache("testWriteEmpty", new byte[]{});

		Path f = cache.resolve("testWriteEmpty");
		assertTrue(Files.exists(f));

		byte[] content = Files.readAllBytes(f);
		assertNotNull(content);
		assertTrue(content.length == 0);

		byte[] read = fileAgent.readCache("testWriteEmpty");
		assertNotNull(read);
		assertTrue(read.length == 0);
	}

	@Test
	public void testOverwrite() throws IOException {
		String data1 = RandomStringUtils.random(1000);
		fileAgent.writeCache("testOverwrite", data1.getBytes());

		String data2 = RandomStringUtils.random(1000);
		fileAgent.writeCache("testOverwrite", data2.getBytes());

		byte[] read = fileAgent.readCache("testOverwrite");
		assertNotNull(read);
		assertNotEquals(data1,  data2);
		assertNotEquals(data1, new String(read));
		assertEquals(data2, new String(read));
	}

	@Test
	public void testRead() {
		byte[] read = fileAgent.readCache("testRead");
		assertNull(read);
	}

	@Test
	public void testReadWrongKey() throws IOException {
		String data = RandomStringUtils.random(1000);
		fileAgent.writeCache("key", data.getBytes());
		byte[] read = fileAgent.readCache("wrongKey");
		assertNull(read);
	}
}

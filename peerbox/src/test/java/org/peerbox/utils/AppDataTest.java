package org.peerbox.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.peerbox.app.Constants;

public class AppDataTest {

	private Path tempPath;
	static boolean first = true;

	@Before
	public void setUp() throws Exception {
		String folderName = RandomStringUtils.randomAlphanumeric(16);
		tempPath = Paths.get(FileUtils.getTempDirectoryPath(), folderName);

		if (first) {
			testDefaultPath();
			first = false;
		}

		AppData.setDataFolder(tempPath);
	}

	@After
	public void tearDown() throws Exception {
		if (Files.exists(tempPath)) {
			FileUtils.deleteDirectory(tempPath.toFile());
		}
	}

	private void testDefaultPath() {
		// default path is in user directory
		Path defaultPath = Paths.get(FileUtils.getUserDirectoryPath(), String.format(".%s", Constants.APP_NAME));
		assertNotNull(AppData.getDataFolder());
		assertEquals(defaultPath, AppData.getDataFolder());
	}

	@Test
	public void testSetDataFolder() {
		AppData.setDataFolder(null);
		assertNull(AppData.getDataFolder());

		AppData.setDataFolder(tempPath);
		assertNotNull(AppData.getDataFolder());
		assertEquals(tempPath, AppData.getDataFolder());

		AppData.setDataFolder(tempPath.resolve("abc"));
		assertNotNull(AppData.getDataFolder());
		assertEquals(tempPath.resolve("abc"), AppData.getDataFolder());
	}

	@Test
	public void testGetDataFolder() {
		assertNotNull(AppData.getDataFolder());
		assertEquals(tempPath, AppData.getDataFolder());
		assertFalse(Files.exists(tempPath));
	}

	@Test
	public void testGetConfigFolder() {
		Path config = tempPath.resolve("config");
		assertEquals(AppData.getConfigFolder(), config);
		assertFalse(Files.exists(config));
	}

	@Test
	public void testGetCacheFolder() {
		Path cache = tempPath.resolve("cache");
		assertEquals(AppData.getCacheFolder(), cache);
		assertFalse(Files.exists(cache));
	}

	@Test
	public void testGetLogFolder() {
		Path log = tempPath.resolve("log");
		assertEquals(AppData.getLogFolder(), log);
		assertFalse(Files.exists(log));
	}

	@Test
	public void testCreateFolders() throws IOException {
		assertFalse(Files.exists(AppData.getDataFolder()));
		assertFalse(Files.exists(AppData.getConfigFolder()));
		assertFalse(Files.exists(AppData.getCacheFolder()));
		assertFalse(Files.exists(AppData.getLogFolder()));

		AppData.createFolders();

		assertTrue(Files.exists(AppData.getDataFolder()));
		assertTrue(Files.exists(AppData.getConfigFolder()));
		assertTrue(Files.exists(AppData.getCacheFolder()));
		assertTrue(Files.exists(AppData.getLogFolder()));
	}

	@Test
	public void testCheckAccess_NotExists() throws IOException {
		try {
			AppData.checkAccess();
			fail("Exception not thrown.");
		} catch (IOException e) {
			// expected
		}

		AppData.createFolders();
		AppData.checkAccess();
	}

}

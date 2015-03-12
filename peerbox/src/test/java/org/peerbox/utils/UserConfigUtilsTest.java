package org.peerbox.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.app.config.UserConfig;

public class UserConfigUtilsTest {

	private static Path originalDataFolder;

	private static Path tempDataFolder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		originalDataFolder = AppData.getDataFolder();

		// use temporary app data folder
		tempDataFolder = Paths.get(FileUtils.getTempDirectoryPath(), "userconfig_test");
		AppData.setDataFolder(tempDataFolder);
		AppData.createFolders();

		FileUtils.cleanDirectory(AppData.getConfigFolder().toFile());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// restore other folder (initial)
		AppData.setDataFolder(originalDataFolder);

		// clear temporary folder
		FileUtils.deleteQuietly(tempDataFolder.toFile());
	}

	@Test
	public void testCreateUserConfig_ByUsername() {
		String sha256 = "96d9632f363564cc3032521409cf22a852f2032eec099ed5967c0d000cec607a"; // John
		String username = "John";
		Path expectedFile = Paths.get(String.format("%s.conf", sha256));

		UserConfig cfg = UserConfigUtils.createUserConfig(username);
		assertNotNull(cfg);
		assertEquals(expectedFile, cfg.getConfigFile().getFileName());

		// lower
		cfg = UserConfigUtils.createUserConfig(username.toLowerCase());
		assertNotNull(cfg);
		assertEquals(expectedFile, cfg.getConfigFile().getFileName());

		// upper
		cfg = UserConfigUtils.createUserConfig(username.toUpperCase());
		assertNotNull(cfg);
		assertEquals(expectedFile, cfg.getConfigFile().getFileName());
	}

	@Test
	public void testCreateUserConfig_ByFile() {
		Path file = Paths.get(FileUtils.getTempDirectoryPath(), "userconfig_test.conf");

		UserConfig cfg = UserConfigUtils.createUserConfig(file);
		assertNotNull(cfg);
		assertEquals(file, cfg.getConfigFile());
	}

	@Test
	public void testGetAllConfigFiles() throws IOException {
		// user 1
		UserConfig cfgJohn = UserConfigUtils.createUserConfig("john");
		cfgJohn.load();
		cfgJohn.setUsername("john");

		// user 2
		UserConfig cfgSteven = UserConfigUtils.createUserConfig("steven");
		cfgSteven.load();
		cfgSteven.setUsername("steven");

		// search files
		Map<String, UserConfig> cfgFiles = UserConfigUtils.getAllConfigFiles();
		assertEquals(2, cfgFiles.size());

		assertTrue(cfgFiles.containsKey("john"));
		assertTrue(cfgFiles.containsKey("steven"));

		assertEquals("john", cfgFiles.get("john").getUsername());
		assertEquals("steven", cfgFiles.get("steven").getUsername());

	}

}

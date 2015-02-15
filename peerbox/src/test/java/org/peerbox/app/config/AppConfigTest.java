package org.peerbox.app.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AppConfigTest {

	private Path configFile;
	private AppConfig userConfig;

	@Before
	public void setUp() throws IOException {
		configFile = Paths.get(FileUtils.getTempDirectoryPath(), "testconfig.conf");
		userConfig = new AppConfig(configFile);
		userConfig.load();
	}

	@After
	public void tearDown() throws IOException {
		Files.deleteIfExists(configFile);
		userConfig = null;
	}

	@Test
	public void testHasApiServerPort() throws IOException {
		userConfig.setApiServerPort(0);
		assertFalse(userConfig.hasApiServerPort());

		userConfig.setApiServerPort(-1);
		assertFalse(userConfig.hasApiServerPort());

		userConfig.setApiServerPort(65536);
		assertFalse(userConfig.hasApiServerPort());

		userConfig.setApiServerPort(1);
		assertTrue(userConfig.hasApiServerPort());

		userConfig.setApiServerPort(47325);
		assertTrue(userConfig.hasApiServerPort());

		userConfig.setApiServerPort(65535);
		assertTrue(userConfig.hasApiServerPort());
	}

	@Test
	public void testSetApiServerPort() throws IOException {
		// invalid ports
		userConfig.setApiServerPort(0);
		assertEquals(userConfig.getApiServerPort(), -1);
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setApiServerPort(-1);
		assertEquals(userConfig.getApiServerPort(), -1);
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setApiServerPort(65536);
		assertEquals(userConfig.getApiServerPort(), -1);
		userConfigAssertPersistence(userConfig, configFile);

		// valid ports
		userConfig.setApiServerPort(1);
		assertEquals(userConfig.getApiServerPort(), 1);
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setApiServerPort(37824);
		assertEquals(userConfig.getApiServerPort(), 37824);
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setApiServerPort(65535);
		assertEquals(userConfig.getApiServerPort(), 65535);
		userConfigAssertPersistence(userConfig, configFile);
	}

	/**
	 * Allows to check that changes made to a config are persistent, i.e. saved on disk in
	 * a property file.
	 * Asserts that two user config instances are equals by comparing them with each
	 * other regarding their state (properties).
	 *
	 * Usage: given an instance a, create a new instance b that reads the config again and compare.
	 *
	 * @param a an instance
	 * @param b another instance
	 * @throws IOException if loading fails
	 */
	private void userConfigAssertPersistence(AppConfig a, Path file) throws IOException {
		AppConfig b = new AppConfig(file);
		b.load();

		assertEquals(a.getApiServerPort(), b.getApiServerPort());
		assertTrue(a.hasApiServerPort() == b.hasApiServerPort());

		assertEquals(a.getConfigFile(), b.getConfigFile());
	}
}

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
import org.peerbox.BaseJUnitTest;

public class AppConfigTest extends BaseJUnitTest {

	private Path configFile;
	private AppConfig appConfig;

	@Before
	public void setUp() throws IOException {
		configFile = Paths.get(FileUtils.getTempDirectoryPath(), "testappconfig.conf");
		appConfig = new AppConfig(configFile);
		appConfig.load();
	}

	@After
	public void tearDown() throws IOException {
		Files.deleteIfExists(configFile);
		appConfig = null;
	}

	@Test
	public void testHasApiServerPort() throws IOException {
		appConfig.setApiServerPort(0);
		assertFalse(appConfig.hasApiServerPort());

		appConfig.setApiServerPort(-1);
		assertFalse(appConfig.hasApiServerPort());

		appConfig.setApiServerPort(65536);
		assertFalse(appConfig.hasApiServerPort());

		appConfig.setApiServerPort(1);
		assertTrue(appConfig.hasApiServerPort());

		appConfig.setApiServerPort(47325);
		assertTrue(appConfig.hasApiServerPort());

		appConfig.setApiServerPort(65535);
		assertTrue(appConfig.hasApiServerPort());
	}

	@Test
	public void testSetApiServerPort() throws IOException {
		// invalid ports
		appConfig.setApiServerPort(0);
		assertEquals(appConfig.getApiServerPort(), -1);
		userConfigAssertPersistence(appConfig, configFile);

		appConfig.setApiServerPort(-1);
		assertEquals(appConfig.getApiServerPort(), -1);
		userConfigAssertPersistence(appConfig, configFile);

		appConfig.setApiServerPort(65536);
		assertEquals(appConfig.getApiServerPort(), -1);
		userConfigAssertPersistence(appConfig, configFile);

		// valid ports
		appConfig.setApiServerPort(1);
		assertEquals(appConfig.getApiServerPort(), 1);
		userConfigAssertPersistence(appConfig, configFile);

		appConfig.setApiServerPort(37824);
		assertEquals(appConfig.getApiServerPort(), 37824);
		userConfigAssertPersistence(appConfig, configFile);

		appConfig.setApiServerPort(65535);
		assertEquals(appConfig.getApiServerPort(), 65535);
		userConfigAssertPersistence(appConfig, configFile);
	}

	@Test
	public void testSetTrayNotification() throws IOException {
		appConfig.setTrayNotification(true);
		assertTrue(appConfig.isTrayNotificationEnabled());

		appConfig.setTrayNotification(false);
		assertFalse(appConfig.isTrayNotificationEnabled());

		appConfig.setTrayNotification(true);
		assertTrue(appConfig.isTrayNotificationEnabled());
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

		assertEquals(a.isTrayNotificationEnabled(), b.isTrayNotificationEnabled());

		assertEquals(a.getConfigFile(), b.getConfigFile());
	}
}

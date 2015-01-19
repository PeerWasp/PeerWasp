package org.peerbox.app.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserConfigTest {

	private Path configFile;
	private UserConfig userConfig;

	@Before
	public void setUp() throws IOException {
		configFile = Paths.get(FileUtils.getTempDirectoryPath(), "testconfig.conf");
		userConfig = new UserConfig(configFile);
		userConfig.load();
	}

	@After
	public void tearDown() {
		userConfig = null;
	}

	@Test(expected=IllegalStateException.class)
	public void testLoad() throws IOException {
		UserConfig cfg = new UserConfig(null);
		cfg.load();
	}

	@Test
	public void testGetConfigFileName() {
		assertEquals(userConfig.getConfigFileName(), configFile);

		UserConfig cfg = new UserConfig(null);
		assertNull(cfg.getConfigFileName());

		Path newPath = Paths.get(FileUtils.getTempDirectoryPath(), "test-config-file.txt");
		cfg = new UserConfig(newPath);
		assertEquals(cfg.getConfigFileName(), newPath);
	}

	@Test
	public void testHasRootPath() throws IOException {
		userConfig.setRootPath(Paths.get(""));
		assertFalse(userConfig.hasRootPath());

		userConfig.setRootPath(Paths.get(" "));
		assertTrue(userConfig.hasRootPath());

		userConfig.setRootPath(Paths.get("/this/is/a/path"));
		assertTrue(userConfig.hasRootPath());

		userConfig.setRootPath(null);
		assertFalse(userConfig.hasRootPath());
	}

	@Test
	public void testSetRootPath() throws IOException {
		userConfig.setRootPath(Paths.get(""));
		assertNull(userConfig.getRootPath());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setRootPath(Paths.get("/this/is/a/Path"));
		assertEquals(userConfig.getRootPath().toString(), "/this/is/a/Path");
		assertNotEquals(userConfig.getRootPath(), "/this/is/a/Path".toLowerCase());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setRootPath(Paths.get("/this/is/a/Path/John Doe "));
		assertEquals(userConfig.getRootPath().toString(), "/this/is/a/Path/John Doe ");
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setRootPath(null);
		assertNull(userConfig.getRootPath());
		userConfigAssertPersistence(userConfig, configFile);
	}

	@Test
	public void testHasUsername() throws IOException {
		userConfig.setUsername("");
		assertFalse(userConfig.hasUsername());

		userConfig.setUsername(" ");
		assertFalse(userConfig.hasUsername());

		userConfig.setUsername("testuser");
		assertTrue(userConfig.hasUsername());

		userConfig.setUsername(null);
		assertFalse(userConfig.hasUsername());
	}

	@Test
	public void testSetUsername() throws IOException {
		userConfig.setUsername("");
		assertNull(userConfig.getUsername());
		userConfigAssertPersistence(userConfig, configFile);

		// TODO: Design decision: username may be not case sensitive but rather
		// (holds for registration, login, ... not necessarily config)

		userConfig.setUsername("TestUser123");
		assertEquals(userConfig.getUsername(), "TestUser123");
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setUsername("  JohnDoe  ");
		assertNotEquals(userConfig.getUsername(), "  JohnDoe  ");
		assertEquals(userConfig.getUsername(), "JohnDoe");
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setUsername(null);
		assertNull(userConfig.getUsername());
		userConfigAssertPersistence(userConfig, configFile);
	}

	@Test
	public void testHasPassword() throws IOException {
		userConfig.setPassword("");
		assertFalse(userConfig.hasPassword());

		userConfig.setPassword(" ");
		assertTrue(userConfig.hasPassword());

		userConfig.setPassword("mySecretPassword");
		assertTrue(userConfig.hasPassword());

		userConfig.setPassword(null);
		assertFalse(userConfig.hasPassword());
	}

	@Test
	public void testSetPassword() throws IOException {
		userConfig.setPassword("");
		assertNull(userConfig.getPassword());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setPassword("mySecret#Password123");
		assertEquals(userConfig.getPassword(), "mySecret#Password123");
		assertNotEquals(userConfig.getPassword(), "mySecret#Password123".toLowerCase());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setPassword(" my secret passwort ");
		assertEquals(userConfig.getPassword(), " my secret passwort ");
		assertNotEquals(userConfig.getPassword(), " my secret passwort ".trim());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setPassword(null);
		assertNull(userConfig.getPassword());
		userConfigAssertPersistence(userConfig, configFile);
	}

	@Test
	public void testHasPin() throws IOException {
		userConfig.setPin("");
		assertFalse(userConfig.hasPin());

		userConfig.setPin(" ");
		assertTrue(userConfig.hasPin());

		userConfig.setPin("myOwnPin");
		assertTrue(userConfig.hasPin());

		userConfig.setPin(null);
		assertFalse(userConfig.hasPin());
	}

	@Test
	public void testSetPin() throws IOException {
		userConfig.setPin("");
		assertNull(userConfig.getPin());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setPin("ThisIs-MyPin");
		assertEquals(userConfig.getPin(), "ThisIs-MyPin");
		assertNotEquals(userConfig.getPin(), "ThisIs-MyPin".toLowerCase());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setPin("   another pin 123 ");
		assertEquals(userConfig.getPin(), "   another pin 123 ");
		assertNotEquals(userConfig.getPin(), "   another pin 123 ".trim());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setPin(null);
		assertNull(userConfig.getPin());
		userConfigAssertPersistence(userConfig, configFile);
	}

	@Test
	public void testSetAutoLogin() throws IOException {
		userConfig.setAutoLogin(true);
		assertTrue(userConfig.isAutoLoginEnabled());
		userConfigAssertPersistence(userConfig, configFile);

		userConfig.setAutoLogin(false);
		assertFalse(userConfig.isAutoLoginEnabled());
		userConfigAssertPersistence(userConfig, configFile);
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
	private void userConfigAssertPersistence(UserConfig a, Path file) throws IOException {
		UserConfig b = new UserConfig(file);
		b.load();

		assertEquals(a.getUsername(), b.getUsername());
		assertTrue(a.hasUsername() == b.hasUsername());

		assertEquals(a.getPassword(), b.getPassword());
		assertTrue(a.hasPassword() == b.hasPassword());

		assertEquals(a.getPin(), b.getPin());
		assertTrue(a.hasPin() == b.hasPin());

		assertEquals(a.getRootPath(), b.getRootPath());
		assertTrue(a.hasRootPath() == b.hasRootPath());

		assertEquals(a.getApiServerPort(), b.getApiServerPort());
		assertTrue(a.hasApiServerPort() == b.hasApiServerPort());

		assertEquals(a.isAutoLoginEnabled(), b.isAutoLoginEnabled());

		assertEquals(a.getConfigFileName(), b.getConfigFileName());

	}

}

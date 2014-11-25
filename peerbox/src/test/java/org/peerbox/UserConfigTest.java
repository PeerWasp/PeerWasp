package org.peerbox;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserConfigTest {
	
	private UserConfig userConfig;
	
	@Before
	public void setUp() throws IOException {
		userConfig = new UserConfig();
	}
	
	@After
	public void tearDown() {
		userConfig = null;
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
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setRootPath(Paths.get("/this/is/a/Path"));
		assertEquals(userConfig.getRootPath().toString(), "/this/is/a/Path");
		assertNotEquals(userConfig.getRootPath(), "/this/is/a/Path".toLowerCase());
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setRootPath(Paths.get("/this/is/a/Path/John Doe "));
		assertEquals(userConfig.getRootPath().toString(), "/this/is/a/Path/John Doe ");
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setRootPath(null);
		assertNull(userConfig.getRootPath());
		userConfigAssertPersistence(userConfig, new UserConfig());
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
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		// TODO: Design decision: username may be not case sensitive but rather 
		// (holds for registration, login, ... not necessarily config)
		
		userConfig.setUsername("TestUser123");
		assertEquals(userConfig.getUsername(), "TestUser123");
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setUsername("  JohnDoe  ");
		assertNotEquals(userConfig.getUsername(), "  JohnDoe  ");
		assertEquals(userConfig.getUsername(), "JohnDoe");
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setUsername(null);
		assertNull(userConfig.getUsername());
		userConfigAssertPersistence(userConfig, new UserConfig());
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
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setPassword("mySecret#Password123");
		assertEquals(userConfig.getPassword(), "mySecret#Password123");
		assertNotEquals(userConfig.getPassword(), "mySecret#Password123".toLowerCase());
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setPassword(" my secret passwort ");
		assertEquals(userConfig.getPassword(), " my secret passwort ");
		assertNotEquals(userConfig.getPassword(), " my secret passwort ".trim());
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setPassword(null);
		assertNull(userConfig.getPassword());
		userConfigAssertPersistence(userConfig, new UserConfig());
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
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setPin("ThisIs-MyPin");
		assertEquals(userConfig.getPin(), "ThisIs-MyPin");
		assertNotEquals(userConfig.getPin(), "ThisIs-MyPin".toLowerCase());
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setPin("   another pin 123 ");
		assertEquals(userConfig.getPin(), "   another pin 123 ");
		assertNotEquals(userConfig.getPin(), "   another pin 123 ".trim());
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setPin(null);
		assertNull(userConfig.getPin());
		userConfigAssertPersistence(userConfig, new UserConfig());
	}

	@Test
	public void testSetAutoLogin() throws IOException {
		userConfig.setAutoLogin(true);
		assertTrue(userConfig.isAutoLoginEnabled());
		userConfigAssertPersistence(userConfig, new UserConfig());
		
		userConfig.setAutoLogin(false);
		assertFalse(userConfig.isAutoLoginEnabled());
		userConfigAssertPersistence(userConfig, new UserConfig());
	}
	
	@Test 
	public void hasApiServerPort() throws IOException {
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
	public void setApiServerPort() throws IOException {
		// invalid ports
		userConfig.setApiServerPort(0);
		assertEquals(userConfig.getApiServerPort(), -1);
		
		userConfig.setApiServerPort(-1);
		assertEquals(userConfig.getApiServerPort(), -1);
		
		userConfig.setApiServerPort(65536);
		assertEquals(userConfig.getApiServerPort(), -1);
		
		// valid ports
		userConfig.setApiServerPort(1);
		assertEquals(userConfig.getApiServerPort(), 1);
		
		userConfig.setApiServerPort(37824);
		assertEquals(userConfig.getApiServerPort(), 37824);
		
		userConfig.setApiServerPort(65535);
		assertEquals(userConfig.getApiServerPort(), 65535);
	}

	@SuppressWarnings("serial")
	@Test
	public void testHasBootstrappingNodes() throws IOException {
		userConfig.setBootstrappingNodes(new ArrayList<String>());
		assertFalse(userConfig.hasBootstrappingNodes());
		
		List<String> list_1 = new ArrayList<String>() {
			{
				add(" ");
			}
		};
		userConfig.setBootstrappingNodes(list_1);
		assertFalse(userConfig.hasBootstrappingNodes());
		
		List<String> list_2 = new ArrayList<String>() {
			{
				add(" ");
				add("localhost");
			}
		};
		userConfig.setBootstrappingNodes(list_2);
		assertTrue(userConfig.hasBootstrappingNodes());
		
		userConfig.setBootstrappingNodes(null);
		assertFalse(userConfig.hasBootstrappingNodes());
	}

	@Test
	public void testSetBootstrappingNodes_empty() throws IOException {
		List<String> list_in = new ArrayList<String>();
		userConfig.setBootstrappingNodes(list_in);
		List<String> list_out = userConfig.getBootstrappingNodes();
		assertTrue(list_out.isEmpty());
		userConfigAssertPersistence(userConfig, new UserConfig());
	}

	@SuppressWarnings("serial")
	@Test
	public void testSetBootstrappingNodes_trim() throws IOException {
		List<String> list_in = new ArrayList<String>() {
			{
				add(" ");
			}
		};
		userConfig.setBootstrappingNodes(list_in);
		List<String> list_out = userConfig.getBootstrappingNodes();
		assertTrue(list_out.isEmpty());
		userConfigAssertPersistence(userConfig, new UserConfig());
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testSetBootstrappingNodes_nodes() throws IOException {
		List<String> list_in = new ArrayList<String>() {
			{
				add(" ");
				add("");
				add(null);
				add("localhost");
				add(" 192.168.1.101  ");
			}
		};
		userConfig.setBootstrappingNodes(list_in);
		List<String> list_out = userConfig.getBootstrappingNodes();
		assertTrue(list_out.size() == 2);
		assertTrue(list_out.contains("localhost"));
		assertTrue(list_out.contains(" 192.168.1.101  ".trim()));
		assertFalse(list_out.contains(" 192.168.1.101  "));
		userConfigAssertPersistence(userConfig, new UserConfig());
	}

	@Test
	public void testSetBootstrappingNodes_null() throws IOException {
		userConfig.setBootstrappingNodes(null);
		List<String> list_out = userConfig.getBootstrappingNodes();
		assertTrue(list_out.isEmpty());
		userConfigAssertPersistence(userConfig, new UserConfig());
	}

	@SuppressWarnings("serial")
	@Test
	public void testAddBootstrapNode() throws IOException {
		List<String> list_in = new ArrayList<String>() {
			{
				add(" ");
				add("localhost");
				add(" 192.168.1.101  ");
			}
		};
		userConfig.setBootstrappingNodes(list_in);
		userConfig.addBootstrapNode(" my-fancy-bootstrap-host-name.com ");
		userConfig.addBootstrapNode("");
		userConfig.addBootstrapNode(" ");
		userConfig.addBootstrapNode(null);
		
		List<String> list_out = userConfig.getBootstrappingNodes();
		assertTrue(list_out.contains("localhost"));
		assertTrue(list_out.contains(" 192.168.1.101  ".trim()));
		assertTrue(list_out.contains(" my-fancy-bootstrap-host-name.com ".trim()));
		assertTrue(list_out.size() == 3);
		userConfigAssertPersistence(userConfig, new UserConfig());
	}

	@SuppressWarnings("serial")
	@Test
	public void testRemoveBootstrapNode() throws IOException {
		List<String> list_in = new ArrayList<String>() {
			{
				add(" ");
				add("localhost");
				add(" 192.168.1.101  ");
				add(" my-fancy-bootstrap-host-name.com ");
			}
		};
		userConfig.setBootstrappingNodes(list_in);
		userConfig.removeBootstrapNode("192.168.1.101");
		userConfig.removeBootstrapNode("");
		userConfig.removeBootstrapNode(" ");
		userConfig.removeBootstrapNode(null);
		
		List<String> list_out = userConfig.getBootstrappingNodes();
		assertTrue(list_out.contains("localhost"));
		assertFalse(list_out.contains(" 192.168.1.101  ".trim()));
		assertTrue(list_out.contains(" my-fancy-bootstrap-host-name.com ".trim()));
		assertTrue(list_out.size() == 2);
		userConfigAssertPersistence(userConfig, new UserConfig());
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
	 */
	private void userConfigAssertPersistence(UserConfig a, UserConfig b) {
		assertEquals(a.getUsername(), b.getUsername());
		assertTrue(a.hasUsername() == b.hasUsername());
		
		assertEquals(a.getPassword(), b.getPassword());
		assertTrue(a.hasPassword() == b.hasPassword());
		
		assertEquals(a.getPin(), b.getPin());
		assertTrue(a.hasPin() == b.hasPin());
		
		assertEquals(a.getRootPath(), b.getRootPath());
		assertTrue(a.hasRootPath() == b.hasRootPath());
		
		assertTrue(a.hasBootstrappingNodes() == b.hasBootstrappingNodes());
		List<String> nodesA = a.getBootstrappingNodes();
		List<String> nodesB = b.getBootstrappingNodes();
		assertTrue(nodesA.size() == nodesB.size());
		for(String n : nodesA) {
			assertTrue(nodesB.contains(n));
		}
	}

}

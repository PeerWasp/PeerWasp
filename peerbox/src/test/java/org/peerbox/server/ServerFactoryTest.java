package org.peerbox.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.BaseJUnitTest;
import org.peerbox.utils.NetUtils;

public class ServerFactoryTest extends BaseJUnitTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateServer() {
		IServer server = ServerFactory.createServer();
		assertNotNull(server);
		assertTrue(server.getPort() >= NetUtils.MIN_PORT);
		assertTrue(server.getPort() <= NetUtils.MAX_PORT);
	}

	@Test
	public void testGetContextMenuDeletePath() {
		assertNotNull(ServerFactory.getContextMenuDeletePath());
		assertEquals("/contextmenu/delete", ServerFactory.getContextMenuDeletePath());
	}

	@Test
	public void testGetContextMenuVersionsPath() {
		assertNotNull(ServerFactory.getContextMenuVersionsPath());
		assertEquals("/contextmenu/versions", ServerFactory.getContextMenuVersionsPath());
	}

	@Test
	public void testGetContextMenuSharePath() {
		assertNotNull(ServerFactory.getContextMenuSharePath());
		assertEquals("/contextmenu/share", ServerFactory.getContextMenuSharePath());
	}

}

package org.peerbox.app.config;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BootstrappingNodesTest {

	private BootstrappingNodes nodes;

	@Before
	public void beforeTest() {
		nodes = new BootstrappingNodes();
	}

	@After
	public void afterTest() {
		nodes = null;
	}

	@Test
	public void testInitial() {
		assertTrue(nodes.getBootstrappingNodes().isEmpty());
		assertFalse(nodes.hasBootstrappingNodes());
		assertNotNull(nodes.getLastNode());
		assertTrue(nodes.getLastNode().isEmpty());
		assertFalse(nodes.hasLastNode());
	}

	@Test
	public void testGetAndSetLastNode() {
		nodes.setLastNode(null);
		assertNotNull(nodes.getLastNode());
		assertEquals(nodes.getLastNode(), "");

		nodes.setLastNode("localhost");
		assertEquals(nodes.getLastNode(), "localhost");

		nodes.setLastNode(" example.org");
		assertEquals(nodes.getLastNode(), "example.org");

		nodes.setLastNode("127.0.0.1 ");
		assertEquals(nodes.getLastNode(), "127.0.0.1");
	}

	@Test
	public void testHasLastNode() {
		nodes.setLastNode("");
		assertFalse(nodes.hasLastNode());

		nodes.setLastNode(" ");
		assertFalse(nodes.hasLastNode());

		nodes.setLastNode("localhost");
		assertTrue(nodes.hasLastNode());

		nodes.setLastNode(null);
		assertFalse(nodes.hasLastNode());
	}

	@Test
	public void testHasBootstrappingNodes() throws IOException {

		assertFalse(nodes.hasBootstrappingNodes());

		nodes.addNode("localhost");

		assertTrue(nodes.hasBootstrappingNodes());

		nodes.removeNode("localhost");

		assertFalse(nodes.hasBootstrappingNodes());

	}

	@Test
	public void testAddNode() {
		assertTrue(nodes.getBootstrappingNodes().isEmpty());

		nodes.addNode("");
		assertTrue(nodes.getBootstrappingNodes().isEmpty());

		nodes.addNode(null);
		assertTrue(nodes.getBootstrappingNodes().isEmpty());

		nodes.addNode(" ");
		assertTrue(nodes.getBootstrappingNodes().isEmpty());

		nodes.addNode("localhost");
		assertEquals(nodes.getBootstrappingNodes().size(), 1);
		assertTrue(nodes.getBootstrappingNodes().contains("localhost"));

		nodes.addNode(" 127.0.0.1 ");
		assertEquals(nodes.getBootstrappingNodes().size(), 2);
		assertTrue(nodes.getBootstrappingNodes().contains("127.0.0.1"));
		assertFalse(nodes.getBootstrappingNodes().contains(" 127.0.0.1 "));
	}

	@Test
	public void testRemoveNode() {
		assertTrue(nodes.getBootstrappingNodes().isEmpty());

		nodes.addNode("localhost");
		nodes.addNode("127.0.0.1");
		nodes.addNode(" example.org ");
		assertEquals(nodes.getBootstrappingNodes().size(), 3);


		nodes.removeNode("example.org");
		assertEquals(nodes.getBootstrappingNodes().size(), 2);
		assertFalse(nodes.getBootstrappingNodes().contains("example.org"));

		nodes.removeNode("127.0.0.1");
		assertEquals(nodes.getBootstrappingNodes().size(), 1);
		assertFalse(nodes.getBootstrappingNodes().contains("127.0.0.1"));

	}

	@Test
	public void testClearNodes() {
		nodes.addNode("localhost");
		nodes.addNode("127.0.0.1");
		nodes.addNode(" example.org ");

		assertEquals(nodes.getBootstrappingNodes().size(), 3);
		nodes.clearNodes();
		assertEquals(nodes.getBootstrappingNodes().size(), 0);
	}

}

package org.peerbox.app.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.peerbox.BaseJUnitTest;

public class BootstrappingNodesFactoryTest extends BaseJUnitTest {

	private BootstrappingNodes nodes;
	private BootstrappingNodesFactory factory;

	private Path basePath;
	private URL defaultNodes;
	private Path nodesFile;
	private Path lastNodeFile;

	@Before
	public void beforeTest() throws IOException {
		factory = new BootstrappingNodesFactory();

		basePath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerWasp_BootstrappingNodesTest");
		if (!Files.exists(basePath)) {
			Files.createDirectories(basePath);
		}
		defaultNodes = createDefaultNodesFile();
		nodesFile = createNodesFile();
		lastNodeFile = createLastNodeFile();
	}

	@After
	public void afterTest() throws IOException {
		nodes = null;
		factory = null;

		FileUtils.deleteDirectory(basePath.toFile());
	}

	@Test
	public void testCreate() throws IOException {
		nodes = factory.create();
		assertNotNull(nodes);

		assertFalse(nodes.hasLastNode());
		assertFalse(nodes.hasBootstrappingNodes());

		factory.load();
		assertFalse(nodes.hasLastNode());
		assertFalse(nodes.hasBootstrappingNodes());
	}

	@Test
	public void testCreate_Default() throws IOException {
		factory.setNodesDefaultUrl(defaultNodes);
		nodes = factory.create();
		factory.load();

		assertTrue(nodes.hasBootstrappingNodes());
		assertEquals(nodes.getBootstrappingNodes().size(), 2);
		assertTrue(nodes.getBootstrappingNodes().contains("localhost"));
		assertTrue(nodes.getBootstrappingNodes().contains("127.0.0.1"));
	}

	@Test
	public void testCreate_Nodes() throws IOException {
		factory.setNodesFile(nodesFile);
		nodes = factory.create();
		factory.load();

		assertTrue(nodes.hasBootstrappingNodes());
		assertEquals(nodes.getBootstrappingNodes().size(), 3);
		assertTrue(nodes.getBootstrappingNodes().contains("localhost"));
		assertTrue(nodes.getBootstrappingNodes().contains("example.org"));
		assertTrue(nodes.getBootstrappingNodes().contains("192.168.1.100"));
	}

	@Test
	public void testCreate_LastNode() throws IOException {
		factory.setLastNodeFile(lastNodeFile);
		nodes = factory.create();
		factory.load();

		assertTrue(nodes.hasLastNode());
		assertEquals(nodes.getLastNode(), "mynode");
	}

	@Test
	public void testLoadAndSave() throws IOException {
		factory.setLastNodeFile(lastNodeFile);
		factory.setNodesFile(nodesFile);
		factory.setNodesDefaultUrl(defaultNodes);
		nodes = factory.create();
		factory.load();

		assertEquals(nodes.getBootstrappingNodes().size(), 4);

		nodes.addNode("testnode");
		factory.save();
		factory.load();
		assertEquals(nodes.getBootstrappingNodes().size(), 5);
		assertTrue(nodes.getBootstrappingNodes().contains("testnode"));

		// removing default node has no effect
		nodes.removeNode("localhost");
		factory.save();
		factory.load();
		assertEquals(nodes.getBootstrappingNodes().size(), 5);
		assertTrue(nodes.getBootstrappingNodes().contains("localhost"));

		// removing non-default node
		nodes.removeNode("example.org");
		factory.save();
		factory.load();
		assertEquals(nodes.getBootstrappingNodes().size(), 4);
		assertFalse(nodes.getBootstrappingNodes().contains("example.org"));

		// last node
		nodes.setLastNode("lastnode");
		factory.save();
		factory.load();
		assertEquals(nodes.getLastNode(), "lastnode");
	}

	private URL createDefaultNodesFile() throws IOException {
		Path f = basePath.resolve("default_nodes");
		String n = "localhost\n" +
				   "127.0.0.1\n";
		Files.write(f, n.getBytes());
		return f.toUri().toURL();
	}

	private Path createNodesFile() throws IOException {
		Path f = basePath.resolve("nodes");
		String n = "localhost\n" +
				   "example.org\n" +
				   "192.168.1.100\n";
		Files.write(f, n.getBytes());
		return f;
	}

	private Path createLastNodeFile() throws IOException {
		Path f = basePath.resolve("lastnode");
		String n = "mynode";
		Files.write(f, n.getBytes());
		return f;
	}
}

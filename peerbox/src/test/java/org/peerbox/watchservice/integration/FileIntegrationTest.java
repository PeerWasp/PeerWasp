package org.peerbox.watchservice.integration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.peerbox.client.ClientNode;
import org.peerbox.client.NetworkStarter;
import org.peerbox.utils.FileTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

public abstract class FileIntegrationTest {
	
	private static final Logger logger = LoggerFactory.getLogger(FileIntegrationTest.class);
	
	private static NetworkStarter network;
	
	protected static ClientNode client;
	
	protected static final int NUMBER_OF_CHARS = 1000*100; // approx. 100kb
	protected static final int WAIT_TIME_SHORT = 30;
	protected static final int WAIT_TIME_LONG = 5*60;
	
	@BeforeClass
	public static final void beforeClass() throws IOException {
//		network = new NetworkStarter();
//		FileUtils.cleanDirectory(network.getBasePath().toFile());
//		network.run();
//		
//		// Get a random client. Operations will be executed on this client
//		client = network.getClientNode(RandomUtils.nextInt(0, network.getNetworkSize()));
	}
	
	@AfterClass
	public static void afterClass() throws IOException {
//		FileUtils.cleanDirectory(network.getBasePath().toFile());
//		network.stop();
	}
	
	@Before
	public void beforeTest() throws IOException {
		network = new NetworkStarter();
		FileUtils.cleanDirectory(network.getBasePath().toFile());
		network.run();
		
		// Get a random client. Operations will be executed on this client
		client = network.getClientNode(RandomUtils.nextInt(0, network.getNetworkSize()));
	}
	
	@After 
	public void afterTest() throws IOException {
		network.stop();
		FileUtils.cleanDirectory(network.getBasePath().toFile());
	}
	
	protected void waitForExists(Path path, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathExistsOnAllNodes(path));
	}
	
	protected void waitForExists(List<Path> paths, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathExistsOnAllNodes(paths));
	}
		
	protected boolean pathExistsOnAllNodes(Path absPath) {
		Path relativePath = client.getRootPath().relativize(absPath);
		for(ClientNode node : network.getClients()) {
			if(!Files.exists(node.getRootPath().resolve(relativePath))) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean pathExistsOnAllNodes(List<Path> absPaths) {
		for(Path p : absPaths) {
			if(!pathExistsOnAllNodes(p)) {
				return false;
			}
		}
		return true;
	}
	
	protected void waitForNotExists(Path path, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathNotExistsOnAllNodes(path));
	}
	
	protected void waitForNotExists(List<Path> paths, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathNotExistsOnAllNodes(paths));
	}
	
	protected boolean pathNotExistsOnAllNodes(Path absPath) {
		Path relativePath = client.getRootPath().relativize(absPath);
		for(ClientNode node : network.getClients()) {
			if(Files.exists(node.getRootPath().resolve(relativePath))) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean pathNotExistsOnAllNodes(List<Path> absPaths) {
		for(Path p : absPaths) {
			if(pathExistsOnAllNodes(p)) {
				return false;
			}
		}
		return true;
	}
	
	protected Path addSingleFolder() throws IOException {
		Path folder = FileTestUtils.createRandomFolder(client.getRootPath());
		
		waitForExists(folder, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return folder;
	}
	
	protected List<Path> addManyFiles() throws IOException {
		List<Path> files = FileTestUtils.createRandomFiles(client.getRootPath(), 100, NUMBER_OF_CHARS);
		
		waitForExists(files, WAIT_TIME_LONG);
		assertSyncClientPaths();
		return files;
	}
	
	
	/**
	 * Asserts that all root paths of all clients have the same content.
	 * @throws IOException 
	 */
	protected void assertSyncClientPaths() throws IOException {
		// compute client index as a reference
		IndexRootPath clientIndex = new IndexRootPath(client.getRootPath());
		Files.walkFileTree(client.getRootPath(), clientIndex);
		
		// compare index with other root paths
		for(ClientNode node : network.getClients()) {
			if(node.equals(client)) {
				continue; // ignore comparison with itself
			}
			
			// compute index of other path
			IndexRootPath indexOther = new IndexRootPath(node.getRootPath());
			Files.walkFileTree(node.getRootPath(), indexOther);
			
			assertSyncPathIndices(clientIndex, indexOther);
		}
		logger.info("Client paths are SYNC!");
	}

	/**
	 * Compares and asserts equality of two indices by looking at the paths and hashes of the content
	 * @param indexThis
	 * @param rootOther
	 * @throws IOException
	 */
	private void assertSyncPathIndices(IndexRootPath indexThis, IndexRootPath indexOther) throws IOException {
		// 1. compare the paths relative to the root path (set difference must be empty)
		Set<Path> difference = new HashSet<Path>(indexThis.getHashes().keySet());
		difference.removeAll(indexOther.getHashes().keySet());
		// log difference
		for (Path relativePath : difference) {
			Path thisPath = indexThis.getRootPath().resolve(relativePath);
			Path otherPath = indexOther.getRootPath().resolve(relativePath);
			logger.error("Different path: {} ({}) <-> {} ({})", 
					thisPath, Files.exists(thisPath) ? "exists" : "not exists", 
					otherPath, Files.exists(otherPath) ? "exists" : "not exists");
		}
		// difference has to be empty for sync folders
		assertTrue(difference.isEmpty());

		// 2. compare the hashes of the paths
		for (java.util.Map.Entry<Path, String> e : indexThis.getHashes().entrySet()) {
			Path relativePath = e.getKey();
			String thisHash = e.getValue();
			String otherHash = indexOther.getHashes().get(relativePath);
			boolean hashesEqual = thisHash.equals(otherHash);
			// log difference
			if (!hashesEqual) {
				Path thisPath = indexThis.getRootPath().resolve(relativePath);
				Path otherPath = indexOther.getRootPath().resolve(relativePath);
				logger.error("Hashes not equal: {} ({}) <-> {} ({})", 
						thisPath, thisHash, otherPath, otherHash);
			}
			// hashes need to be equal for sync folders
			assertTrue(hashesEqual);
		}
	}
	
	protected class IndexRootPath extends SimpleFileVisitor<Path> {
		private Path rootPath;
		private SortedMap<Path, String> pathToHash;

		public IndexRootPath(Path rootPath) {
			this.rootPath = rootPath;
			this.pathToHash = new TreeMap<Path, String>();
		}
		
		public Path getRootPath() {
			return rootPath;
		}

		public SortedMap<Path, String> getHashes() {
			return pathToHash;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
				throws IOException {
			Path relative = rootPath.relativize(dir);
			pathToHash.put(relative, "");
			return super.preVisitDirectory(dir, attrs);
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			String hash = com.google.common.io.Files.hash(file.toFile(), Hashing.sha256()).toString();
			Path relative = rootPath.relativize(file);
			pathToHash.put(relative, hash);
			return super.visitFile(file, attrs);
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			logger.warn("visitFileFailed: {}", exc);
			return super.visitFileFailed(file, exc);
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return super.postVisitDirectory(dir, exc);
		}

	}
	
}

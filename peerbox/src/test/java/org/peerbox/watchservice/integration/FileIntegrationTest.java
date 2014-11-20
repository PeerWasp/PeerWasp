package org.peerbox.watchservice.integration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.peerbox.client.DummyNetwork;
import org.peerbox.client.ITestNetwork;
import org.peerbox.client.NetworkStarter;
import org.peerbox.utils.FileTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

public abstract class FileIntegrationTest {
	
	protected static final Logger logger = LoggerFactory.getLogger(FileIntegrationTest.class);
	
	private static NetworkStarter network;
	protected static Path masterRootPath;
	
//	protected static final int NUMBER_OF_CHARS = 1000*100; // approx. 100kb
	protected static final int NUMBER_OF_CHARS = 10;
	protected static final int WAIT_TIME_SHORT = 30;
	protected static final int WAIT_TIME_LONG = 120;
	protected static final int WAIT_TIME_STRESSTEST = 600;
	
	
//	@BeforeClass
//	public static void beforeClass() throws IOException {
//		// setup network
//		network = new NetworkStarter();
////		network = new DummyNetwork();
////		FileUtils.cleanDirectory(network.getBasePath().toFile());
//		
//		// start running
//		network.start();
//		
//		// select random path as master path (operations will be executed within this path)
//		masterRootPath = network.getRootPaths().get(
//				RandomUtils.nextInt(0, network.getRootPaths().size()));
//	}
//	
//	@AfterClass
//	public static void afterClass() throws IOException {
//		network.stop();
////		FileUtils.cleanDirectory(network.getBasePath().toFile());
//	}
	protected NetworkStarter getNetwork(){
		return network;
	}

	@Before 
	public void beforeTest() throws IOException {
		// setup network
		network = new NetworkStarter();
//		network = new DummyNetwork();
		FileUtils.cleanDirectory(network.getBasePath().toFile());
		
		// start running
		network.start();
		
		// select random path as master path (operations will be executed within this path)
		masterRootPath = network.getRootPaths().get(0);
				//RandomUtils.nextInt(0, network.getRootPaths().size()));
		
	}
	
	@After
	public void afterTest() throws IOException {
		network.stop();
//		FileUtils.cleanDirectory(network.getBasePath().toFile());
	}
	
	protected Path addSingleFolder() throws IOException {
		Path folder = FileTestUtils.createRandomFolder(masterRootPath);
		
		waitForExists(folder, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return folder;
	}

	protected Path addSingleFile() throws IOException {
		Path file = FileTestUtils.createRandomFile(masterRootPath, NUMBER_OF_CHARS);
		
		waitForExists(file, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return file;
	}
	
	protected Path addSingleFile(Path dstFolder) throws IOException {
		Path file = FileTestUtils.createRandomFile(dstFolder, NUMBER_OF_CHARS);
		
		waitForExists(file, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return file;
	}

	protected List<Path> addManyFiles(int nrFiles, int toWait) throws IOException {
		List<Path> files = FileTestUtils.createRandomFiles(masterRootPath, nrFiles, 100);
		
		waitForExists(files, toWait);
		assertSyncClientPaths();
		return files;
	}
	
	protected List<Path> addManyFiles() throws IOException {
		return addManyFiles(200, WAIT_TIME_LONG);
	}
	
	protected List<Path> addManyFiles(Path dirPath) throws IOException {
		List<Path> files = FileTestUtils.createRandomFiles(dirPath, 100, NUMBER_OF_CHARS);
		waitForExists(files, WAIT_TIME_LONG);
		assertSyncClientPaths();
		return files;
	}

	protected List<Path> addSingleFileInFolder() throws IOException {
		List<Path> files = FileTestUtils.createFolderWithFiles(masterRootPath, 1, NUMBER_OF_CHARS);
		
		waitForExists(files, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return files;
	}

	protected List<Path> addManyFilesInFolder() throws IOException {
		List<Path> files = FileTestUtils.createFolderWithFiles(masterRootPath, 100, NUMBER_OF_CHARS);
		
		waitForExists(files, WAIT_TIME_LONG);
		assertSyncClientPaths();
		return files;
	}

	protected List<Path> addManyFilesInManyFolders() throws IOException {
		List<Path> files = new ArrayList<>();
		int numFolders = 10;
		int numFilesPerFolder = 10;
		for(int i = 0; i < numFolders; ++i) {
			List<Path> f = FileTestUtils.createFolderWithFiles(masterRootPath, numFilesPerFolder, NUMBER_OF_CHARS);
			files.addAll(f);
		}
		
		waitForExists(files, WAIT_TIME_STRESSTEST);		
		assertSyncClientPaths();
		return files;
	}

	protected void waitForExists(Path path, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathExistsOnAllNodes(path));
	}
	
	protected void waitForExistsLocally(Path path, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathExistsLocally(path));
	}
	
	protected void waitIfNotExist(Path path, int seconds){
		H2HWaiter waiter = new H2HWaiter(seconds);
		while(!pathExistsOnAllNodes(path)){
			waiter.tickASecond();
		}
	}
	
	protected void waitForExists(List<Path> paths, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathExistsOnAllNodes(paths));
	}
		
	private boolean pathExistsOnAllNodes(Path absPath) {
		Path relativePath = masterRootPath.relativize(absPath);
		for(Path rp : network.getRootPaths()) {
			if(!Files.exists(rp.resolve(relativePath))) {
				logger.debug("Missing {}", relativePath);
				return false;
			}
		}
		return true;
	}
	
	private boolean pathExistsOnAllNodes(List<Path> absPaths) {
		for(Path p : absPaths) {
			if(!pathExistsOnAllNodes(p)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean pathExistsLocally(Path path){
		return path.toFile().exists();
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
	
	private boolean pathNotExistsOnAllNodes(Path absPath) {
		Path relativePath = masterRootPath.relativize(absPath);
		for(Path rp : network.getRootPaths()) {
			if(Files.exists(rp.resolve(relativePath))) {
				return false;
			}
		}
		return true;
	}
	
	private boolean pathNotExistsOnAllNodes(List<Path> absPaths) {
		for(Path p : absPaths) {
			if(!pathNotExistsOnAllNodes(p)) {
				return false;
			}
		}
		return true;
	}
	
	protected void waitForUpdate(Path path, int seconds) throws IOException {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathEqualsOnAllNodes(path));
	}
	
	protected void waitForUpdate(List<Path> paths, int seconds) throws IOException {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathEqualsOnAllNodes(paths));
	}
	
	protected void waitForContentEquals(Path source, byte[] content, int seconds) throws IOException{
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!contentIsRecoveredLocally(source, content));
	}
	
	private boolean contentIsRecoveredLocally(Path source, byte[] content) throws IOException{
		byte[] newContent = Files.readAllBytes(source);
		System.out.println(new String(content));
		System.out.println(new String(newContent));
		return Arrays.equals(content, newContent);
	}
	
	private boolean pathEqualsOnAllNodes(Path absPath) throws IOException {
		String thisHash = com.google.common.io.Files.hash(absPath.toFile(), Hashing.sha256()).toString();
		Path relativePath = masterRootPath.relativize(absPath);
		for(Path rp : network.getRootPaths()) {
			Path otherPath = rp.resolve(relativePath);
			// make sure path exists already.
			if(!Files.exists(otherPath)) {
				return false;
			}
			// check hashes
			String otherHash = com.google.common.io.Files.hash(otherPath.toFile(), Hashing.sha256()).toString();
			if(!thisHash.equals(otherHash)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean pathEqualsOnAllNodes(List<Path> paths) throws IOException {
		for(Path p : paths) {
			if(!pathEqualsOnAllNodes(p)) {
				return false;
			}
		}
		return true;
	}
			
	/**
	 * Asserts that all root paths of all clients have the same content.
	 * @throws IOException 
	 */
	protected void assertSyncClientPaths() throws IOException {
		// compute client index as a reference
		IndexRootPath clientIndex = new IndexRootPath(masterRootPath);
		Files.walkFileTree(masterRootPath, clientIndex);
		
		// compare index with other root paths
		for(Path rp : network.getRootPaths()) {
			if(rp.equals(masterRootPath)) {
				continue; // ignore comparison with itself
			}
			
			// compute index of other path
			IndexRootPath indexOther = new IndexRootPath(rp);
			Files.walkFileTree(rp, indexOther);
			
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

	private class IndexRootPath extends SimpleFileVisitor<Path> {
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

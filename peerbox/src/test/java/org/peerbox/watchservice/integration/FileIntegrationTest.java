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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.utils.H2HWaiter;
import org.junit.After;
import org.junit.Before;
import org.peerbox.BaseJUnitTest;
import org.peerbox.client.ClientNode;
import org.peerbox.client.NetworkStarter;
import org.peerbox.testutils.FileTestUtils;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IAction;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.peerbox.watchservice.states.ExecutionHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

/**
 * @author Claudio
 * 
 * This class provides useful functions for integration tests 
 * with two peers like adding and deleting files and folders,
 * updating files, waiting for file existence or removal and
 * in particular to check if the internal data structures are
 * in a well-defined state on both client when the test is
 * supposed to end.
 */
public abstract class FileIntegrationTest extends BaseJUnitTest {

	protected static final Logger logger = LoggerFactory.getLogger(FileIntegrationTest.class);

	private static NetworkStarter network;
	protected static Path masterRootPath;
	protected static Path clientRootPath;

	protected static final int NUMBER_OF_CHARS = 10;
	protected static final int WAIT_TIME_VERY_SHORT = 5;
	protected static final int WAIT_TIME_SHORT = 60;
	protected static final int WAIT_TIME_LONG = 240;
	protected static final int WAIT_TIME_STRESSTEST = 60 * 30;

	protected TestPeerWaspConfig config = new TestPeerWaspConfig();
	@Before
	public void beforeTest() throws IOException {

		network = new NetworkStarter();
		
		FileUtils.cleanDirectory(network.getBasePath().toFile());

		network.start();

		// select client-0/ as master (operations will be executed within this path)
		masterRootPath = network.getRootPaths().get(0);
		clientRootPath = network.getRootPaths().get(1);
	}

	@After
	public void afterTest() throws IOException {
		logger.debug("Stop!");
		network.stop();
		network = null;
	}

	protected NetworkStarter getNetwork(){
		return network;
	}
	
	protected Path addFolder() throws IOException {
		Path folder = FileTestUtils.createRandomFolder(masterRootPath);
		waitForExists(folder, WAIT_TIME_SHORT);
		return folder;
	}
	
	protected List<Path> addFolders(int numFolders) throws IOException {
		List<Path> folders = FileTestUtils.createRandomFolders(masterRootPath, numFolders);
		waitForExists(folders, WAIT_TIME_LONG);
		return folders;
	}

	protected Path addFile() throws IOException {
		return addFile(NUMBER_OF_CHARS);
	}


	protected Path addFile(int size) throws IOException {
		return addFile(size, true);
	}
	
	protected Path addFile(boolean waitForExists) throws IOException {
		return addFile(NUMBER_OF_CHARS, waitForExists);
	}
	
	private Path addFile(int size, boolean waitForExists) throws IOException {
		Path file = FileTestUtils.createRandomFile(masterRootPath, size);
		if(waitForExists){
			waitForExists(file, WAIT_TIME_SHORT);
		}
		return file;
	}

	protected Path addFileToDestination(Path dstFolder) throws IOException {
		Path file = FileTestUtils.createRandomFile(dstFolder, NUMBER_OF_CHARS);

		waitForExists(file, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		return file;
	}

	protected List<Path> addFiles(int nrFiles, int toWait) throws IOException {
		List<Path> files = FileTestUtils.createRandomFiles(masterRootPath, nrFiles, 100);

		waitForExists(files, toWait);
		return files;
	}

	protected List<Path> addFiles(Path dirPath) throws IOException {
		List<Path> files = FileTestUtils.createRandomFiles(dirPath, 100, NUMBER_OF_CHARS);
		waitForExists(files, WAIT_TIME_LONG);
		assertSyncClientPaths();
		return files;
	}

	protected List<Path> addSingleFileInFolder() throws IOException {
		List<Path> files = FileTestUtils.createFolderWithFiles(masterRootPath, 1, NUMBER_OF_CHARS);

		waitForExists(files, WAIT_TIME_SHORT);
		return files;
	}

	protected List<Path> addSingleFileInManyFolders(int nrFolders) throws IOException {
		List<Path> files = new ArrayList<Path>();
		for(int i = 0; i < nrFolders; i++){
			files.addAll(FileTestUtils.createFolderWithFiles(masterRootPath, 1, NUMBER_OF_CHARS));
		}
		waitForExists(files, WAIT_TIME_LONG);
		return files;
	}


	protected List<Path> addManyFilesInFolder(int nrFiles) throws IOException {
		return addManyFilesInManyFolders(1, nrFiles); //FileTestUtils.createFolderWithFiles(masterRootPath, 10, NUMBER_OF_CHARS);
	}

	protected List<Path> addManyFilesInManyFolders(int nrFolders, int nrFilesPerFolder) throws IOException {
		List<Path> files = new ArrayList<>();

		for(int i = 0; i < nrFolders; ++i) {
			List<Path> f = FileTestUtils.createFolderWithFiles(masterRootPath, nrFilesPerFolder, NUMBER_OF_CHARS);
			files.addAll(f);
		}

		waitForExists(files, WAIT_TIME_LONG);
		return files;
	}

	protected static void deleteSingleFile(Path filePath, boolean waitForNotExists) throws IOException {
		deleteFileOnClient(filePath, 0);
		if(waitForNotExists){
			waitForNotExists(filePath, WAIT_TIME_SHORT);
		}
	}
	protected static void deleteSingleFile(Path filePath) throws IOException{
		deleteSingleFile(filePath, false);
	}

	protected void deleteManyFiles(List<Path> files) throws IOException {

		for(Path file : files){
			deleteFileOnClient(file, 0);
		}

		waitForNotExists(files, WAIT_TIME_LONG);
	}




	protected void updateSingleFile(Path f, boolean wait) throws IOException {
		FileTestUtils.writeRandomData(f, 100000);
		if(wait){
			waitForUpdate(f, WAIT_TIME_SHORT);
		}

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

	protected void waitForNotExistsLocally(List<Path> paths, int seconds){
		H2HWaiter waiter = new H2HWaiter(seconds);
		do{
			waiter.tickASecond();
		} while(!pathNotExistsLocally(paths));
	}

	protected void waitForNotExistsLocally(Path path, int seconds){
		H2HWaiter waiter = new H2HWaiter(seconds);
		do{
			waiter.tickASecond();
		} while(!pathNotExistsLocally(path));
	}

	protected void waitForExists(List<Path> paths, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathExistsOnAllNodes(paths));
	}
	
	private static void deleteFileOnClient(Path filePath, int i) {

		assertTrue(network.getClients().size() == 2);
		FileEventManager manager = network.getClientNode(0).getFileEventManager();

		logger.debug("Delete file: {}", filePath);
		logger.debug("Manager ID: {}", manager.hashCode());
		manager.onLocalFileHardDelete(filePath);
		sleepMillis(10);
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
				logger.debug("Missing {}", p);
				return false;
			}
		}
		return true;
	}

	private boolean pathExistsLocally(Path path){
		return path.toFile().exists();
	}

	private boolean pathNotExistsLocally(List<Path> paths){
		for(Path p : paths) {
			if(!pathNotExistsLocally(p)) {
				return false;
			}
		}
		return true;
	}

	private boolean pathNotExistsLocally(Path path){
		return !path.toFile().exists();
	}

	protected static void waitForNotExists(Path path, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathNotExistsOnAllNodes(path));
	}

	protected static void waitForNotExists(List<Path> paths, int seconds) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathNotExistsOnAllNodes(paths));
	}
	
	protected static void waitForActionQueueEmpty(BlockingQueue<FileComponent> collection, int seconds){
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
			Vector<FileComponent> vec = new Vector<FileComponent>(collection);
			if(vec.size() != 0){
				for(int i = 0; i < vec.size(); i++){
					logger.debug("Pending in queue: {}. {}:{}", i, vec.get(i).getPath(), vec.get(i).getAction().getCurrentState());
				}
			}
		} while(!isQueueEmpty(collection));
	}
	
	protected static void waitForAsyncHandlesEmpty(BlockingQueue<ExecutionHandle> collection, int seconds){
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
			Vector<ExecutionHandle> vec = new Vector<ExecutionHandle>(collection);
			if(vec.size() != 0){
				for(int i = 0; i < collection.size(); i++){
					FileComponent file = vec.get(i).getAction().getFile();
					logger.debug("Pending in exec: {}. {}:{}", i, file.getPath(), file.getAction().getCurrentState());
				}
			}
		} while(!isQueueEmpty(collection));
	}

	private static boolean isQueueEmpty(BlockingQueue queue) {
		if(queue.size() == 0){
			return true;
		} else {
			return false;
		}
	}

	private static boolean pathNotExistsOnAllNodes(Path absPath) {
		Path relativePath = masterRootPath.relativize(absPath);
		for(Path rp : network.getRootPaths()) {
			if(Files.exists(rp.resolve(relativePath))) {
				return false;
			}
		}
		return true;
	}

	private static boolean pathNotExistsOnAllNodes(List<Path> absPaths) {
		for(Path p : absPaths) {
			if(!pathNotExistsOnAllNodes(p)) {
				logger.debug("Missing {}", p);
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

	protected void assertSyncClientPaths() throws IOException {
		assertSyncClientPaths(true);
	}
	/**
	 * Asserts that all root paths of all clients have the same content.
	 * @throws IOException
	 */
	protected void assertSyncClientPaths(boolean compareTwoway) throws IOException {

		// compute client index as a reference
		IndexRootPath clientIndex = new IndexRootPath(masterRootPath);
		Files.walkFileTree(masterRootPath, clientIndex);

		// compare index with other root paths
		for(Path rp : network.getRootPaths()) {
			if(rp.equals(masterRootPath))
				continue; // ignore comparison with itself

			IndexRootPath indexOther = new IndexRootPath(rp);
			Files.walkFileTree(rp, indexOther);
			assertSyncPathIndices(clientIndex, indexOther);
			
			if(compareTwoway)
				assertSyncPathIndices(indexOther, clientIndex);
		}
		logger.info("Client paths are equal!");
	}

	protected void assertRootContains(int nrFiles) throws IOException {
		IndexRootPath clientIndex = new IndexRootPath(masterRootPath);
		Files.walkFileTree(masterRootPath, clientIndex);
		int contained = clientIndex.getHashes().size();
		logger.info("Root contains/expected: {}/{}", contained, nrFiles + 1);
		if(nrFiles == -1){
			return;
		}
		assertTrue(contained == nrFiles + 1);
	}

	protected void assertQueuesAreEmpty(){
		List<ClientNode> clients = network.getClients();
		for(ClientNode client : clients){
			BlockingQueue<FileComponent> queue = client.getFileEventManager().getFileComponentQueue().getQueue();
			BlockingQueue<ExecutionHandle> execs = client.getActionExecutor().getFailedJobs();
			
			waitForActionQueueEmpty(queue, WAIT_TIME_LONG);
			waitForAsyncHandlesEmpty(execs, WAIT_TIME_LONG);
			
			assertSetMultimapsAreEmpty(client.getFileEventManager().getFileTree());
		}
	}

	private static void assertSetMultimapsAreEmpty(IFileTree fileTree) {
		for(Map.Entry<String, FileComponent> entry : fileTree.getCreatedByContentHash().entries())
			logger.trace("Created file left: {}", entry.getValue().getPath());
		
		for(Map.Entry<String, FileComponent> entry : fileTree.getDeletedByContentHash().entries())
			logger.trace("Deleted file left: {}", entry.getValue().getPath());
		
		for(Map.Entry<String, FolderComposite> entry : fileTree.getCreatedByStructureHash().entries())
			logger.trace("Created folder left: {}", entry.getValue().getPath());
		
		for(Map.Entry<String, FolderComposite> entry : fileTree.getDeletedByStructureHash().entries())
			logger.trace("Deleted file left: {}", entry.getValue().getPath());
		
		assertTrue(fileTree.getCreatedByContentHash().size() == 0);
		assertTrue(fileTree.getCreatedByStructureHash().size() == 0);
		assertTrue(fileTree.getDeletedByContentHash().size() == 0);
		assertTrue(fileTree.getDeletedByStructureHash().size() == 0);
	}

	/**
	 * Compares and asserts equality of two indices by looking at the paths and hashes of the content
	 * @param indexThis
	 * @param rootOther
	 * @throws IOException
	 */
	private void assertSyncPathIndices(IndexRootPath indexThis, IndexRootPath indexOther) throws IOException {
		compareContentByPaths(indexThis, indexOther);
		compareContentByHashes(indexThis, indexOther);
	}

	private void compareContentByHashes(IndexRootPath indexThis,
			IndexRootPath indexOther) {
		
		for (java.util.Map.Entry<Path, String> e : indexThis.getHashes().entrySet()) {
			Path relativePath = e.getKey();
			String thisHash = e.getValue();
			String otherHash = indexOther.getHashes().get(relativePath);
			boolean hashesEqual = thisHash.equals(otherHash);
			
			if (!hashesEqual) {
				Path thisPath = indexThis.getRootPath().resolve(relativePath);
				Path otherPath = indexOther.getRootPath().resolve(relativePath);
				logger.error("Hashes not equal: {} ({}) <-> {} ({})",
						thisPath, thisHash, otherPath, otherHash);
			}
			
			assertTrue(hashesEqual);
		}
	}

	private void compareContentByPaths(IndexRootPath indexThis,
			IndexRootPath indexOther) {
		Set<Path> difference = new HashSet<Path>(indexThis.getHashes().keySet());
		difference.removeAll(indexOther.getHashes().keySet());

		for (Path relativePath : difference) {
			Path thisPath = indexThis.getRootPath().resolve(relativePath);
			Path otherPath = indexOther.getRootPath().resolve(relativePath);
			logger.error("Different path: {} ({}) <-> {} ({})",
					thisPath, Files.exists(thisPath) ? "exists" : "not exists",
					otherPath, Files.exists(otherPath) ? "exists" : "not exists");
		}
		
		assertTrue(difference.isEmpty());
	}

	/**
	 * Wait the defined time interval. Useful to guarantee different timestamps in
	 * milliseconds if events are programatically created. Furthermore allows to wait
	 * for a cleaned action queue if ActionExecutor.ACTION_TIME_TO_WAIT * 2 is passed
	 * as millisToSleep
	 */
	public static void sleepMillis(long millisToSleep){
		try {
			Thread.sleep(millisToSleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
			logger.debug("Add path: {}", file);
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

	/**
	 * This method verifies the following properties on both peers:
	 * 1.) The root folders are synchronized, i.e. their contents are
	 * equal.
	 * 2.) The internal data structures are empty (no pending executing actions,
	 * no pending failed actions, no move candidates left in the various SetMultimaps)
	 * 3.) The root folders contain recursively the defined number of files. this
	 * ensures that no files are missing or unexpectedly existing on both peers.
	 * 
	 * @param existingFiles the number of recursively expected files contained in the root folder.
	 * @throws IOException
	 */
	protected void assertCleanedUpState(int existingFiles) throws IOException {
		assertSyncClientPaths(true);
		assertQueuesAreEmpty();
		assertRootContains(existingFiles);
	}

	protected void assertCleanedUpState(int existingFiles, boolean compareTwoway) throws IOException {
		assertSyncClientPaths(compareTwoway);
		assertQueuesAreEmpty();
		assertRootContains(existingFiles);
	}

	protected void waitForSynchronized(Path path, int seconds, boolean sync) {
		H2HWaiter waiter = new H2HWaiter(seconds);
		do {
			waiter.tickASecond();
		} while(!pathIsSynchronized(path, sync));
	}

	protected boolean pathIsSynchronized(Path path, boolean sync){
		IFileTree fileTree = getNetwork().getClients().get(0).getFileEventManager().getFileTree();
		if(sync){
			if(fileTree.getFile(path) != null && !fileTree.getFile(path).isSynchronized()){
				return false;
			}
		} else {
			if(fileTree.getFile(path) != null && fileTree.getFile(path).isSynchronized()){
				return false;
			}
		}
		return true;
	}

}


package org.peerbox.app.manager.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.app.manager.node.INodeManager;


public class FileManagerTest extends BaseJUnitTest {

	private Path rootPath;
	private Path file;
	private UserConfig userConfig;

	private IH2HNode h2hNode;
	private INodeManager nodeManager;

	private org.hive2hive.core.api.interfaces.IFileManager h2hFileManager;
	private IFileManager fileManager;

	// process to return
	private IProcessComponent<Void> process;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		rootPath = Paths.get(FileUtils.getTempDirectoryPath(), "PeerWasp_TestRootPath");
		file = rootPath.resolve("aFile.txt");

		userConfig = Mockito.mock(UserConfig.class);
		Mockito.stub(userConfig.getRootPath()).toReturn(rootPath);

		nodeManager = Mockito.mock(INodeManager.class);
		h2hNode = Mockito.mock(IH2HNode.class);
		h2hFileManager = Mockito.mock(org.hive2hive.core.api.interfaces.IFileManager.class);
		process = Mockito.mock(IProcessComponent.class);

		Mockito.stub(h2hNode.getFileManager()).toReturn(h2hFileManager);
		Mockito.stub(nodeManager.getNode()).toReturn(h2hNode);

		fileManager = new FileManager(nodeManager, userConfig);
	}

	@After
	public void tearDown() throws Exception {
		nodeManager = null;
		fileManager = null;
	}

	@Test
	public void testAdd() throws NoSessionException, NoPeerConnectionException {
		Mockito.stub(h2hFileManager.createAddProcess(any())).toReturn(process);
		ProcessHandle<Void> p = fileManager.add(file);
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(process, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createAddProcess(file.toFile());
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testUpdate() throws NoSessionException, NoPeerConnectionException {
		Mockito.stub(h2hFileManager.createUpdateProcess(any())).toReturn(process);
		ProcessHandle<Void> p = fileManager.update(file);
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(process, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createUpdateProcess(file.toFile());
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testDelete() throws NoSessionException, NoPeerConnectionException, IllegalArgumentException {
		Mockito.stub(h2hFileManager.createDeleteProcess(any())).toReturn(process);
		ProcessHandle<Void> p = fileManager.delete(file);
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(process, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createDeleteProcess(file.toFile());
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testMove() throws NoSessionException, NoPeerConnectionException {
		Mockito.stub(h2hFileManager.createMoveProcess(any(), any())).toReturn(process);
		Path dst = rootPath.resolve("moveDst.txt");
		ProcessHandle<Void> p = fileManager.move(file, dst);
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(process, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createMoveProcess(file.toFile(), dst.toFile());
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testDownload() throws NoPeerConnectionException, NoSessionException, IllegalArgumentException {
		Mockito.stub(h2hFileManager.createDownloadProcess(any())).toReturn(process);
		ProcessHandle<Void> p = fileManager.download(file);
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(process, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createDownloadProcess(file.toFile());
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testRecover() throws NoSessionException, NoPeerConnectionException {
		Mockito.stub(h2hFileManager.createRecoverProcess(any(), any())).toReturn(process);
		IVersionSelector version = new IVersionSelector() {
			@Override
			public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
				return null;
			}
			@Override
			public String getRecoveredFileName(String fullName, String name, String extension) {
				return null;
			}
		};

		ProcessHandle<Void> p = fileManager.recover(file, version);
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(process, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createRecoverProcess(file.toFile(), version);
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testShare() throws NoSessionException, NoPeerConnectionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException {
		Mockito.stub(h2hFileManager.createShareProcess(any(), any(), any())).toReturn(process);
		String user = "otherUser";
		PermissionType permission = PermissionType.READ;
		ProcessHandle<Void> p = fileManager.share(file, user, permission);
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(process, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createShareProcess(file.toFile(), user, permission);
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testListFiles() throws NoPeerConnectionException, NoSessionException {
		@SuppressWarnings("unchecked")
		IProcessComponent<FileNode> listFiles = (IProcessComponent<FileNode>)Mockito.mock(IProcessComponent.class);
		Mockito.stub(h2hFileManager.createFileListProcess()).toReturn(listFiles);

		ProcessHandle<FileNode> p = fileManager.listFiles();
		assertNotNull(p);
		assertNotNull(p.getProcess());
		assertEquals(listFiles, p.getProcess());

		Mockito.verify(h2hFileManager,  times(1)).createFileListProcess();
		Mockito.verifyNoMoreInteractions(h2hFileManager);
	}

	@Test
	public void testExistsRemote() throws IOException {
		// see FileManagerNetworkTest
	}

	@Test
	public void testIsSmallFile() throws IOException {
		IFileConfiguration fileConf = FileConfiguration.createDefault();
		Mockito.stub(nodeManager.getFileConfiguration()).toReturn(fileConf);

		int size = -1 + fileConf.getMaxFileSize().intValue();
		String content = RandomStringUtils.randomAlphanumeric(size);
		Path smallFile = rootPath.resolve("smallFile.txt");
		FileUtils.writeStringToFile(smallFile.toFile(), content);

		assertTrue(fileManager.isSmallFile(smallFile));
		assertFalse(fileManager.isLargeFile(smallFile));

		Files.deleteIfExists(smallFile);
	}

	@Test
	public void testIsLargeFile() throws IOException {
		IFileConfiguration fileConf = FileConfiguration.createDefault();
		Mockito.stub(nodeManager.getFileConfiguration()).toReturn(fileConf);

		int size = 1 + fileConf.getMaxFileSize().intValue();
		String content = RandomStringUtils.randomAlphanumeric(size);
		Path largeFile = rootPath.resolve("smallFile.txt");
		FileUtils.writeStringToFile(largeFile.toFile(), content);

		assertFalse(fileManager.isSmallFile(largeFile));
		assertTrue(fileManager.isLargeFile(largeFile));

		Files.deleteIfExists(largeFile);
	}

}

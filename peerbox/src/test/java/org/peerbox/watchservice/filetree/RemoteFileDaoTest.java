package org.peerbox.watchservice.filetree;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hive2hive.core.processes.files.list.FileNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.watchservice.PathUtils;
import org.peerbox.watchservice.filetree.persistency.DaoUtils;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao.FileNodeAttr;

public class RemoteFileDaoTest {

	private Path basePath;
	private Path dbFile;
	private RemoteFileDao dao;

	@Before
	public void setUp() throws Exception {
		basePath = Paths.get(FileUtils.getTempDirectoryPath(), RandomStringUtils.randomAlphabetic(12));
		dbFile = Paths.get(FileUtils.getTempDirectoryPath(),
				String.format("%s.testdb", RandomStringUtils.randomAlphabetic(12)));

		DataSource dataSource = DaoUtils.createDataSource(dbFile.toString());
		dao = new RemoteFileDao(dataSource);
	}

	@After
	public void tearDown() throws Exception {
		// physical db file has suffix ".mv.db"
		Files.deleteIfExists(Paths.get(dbFile.toString() + ".mv.db"));
	}

	@Test
	public void testDao() {
		dao.createTable();

		List<FileNodeAttr> resultNodes = null;

		List<FileNode> nodes = createNodeList(10);

		// store and retrieve
		dao.persistAndReplaceFileNodes(nodes);
		resultNodes = dao.getFileNodeAttributes();

		// check content
		checkGivenAndPersisted(resultNodes, nodes);

		// remove some nodes
		nodes.remove(8);
		nodes.remove(5);
		nodes.remove(3);
		// add another node
		Path newFile = basePath.resolve(RandomStringUtils.randomAlphabetic(12));
		FileNode newNode = createFileNode(newFile, true, "new".getBytes());
		nodes.add(newNode);
		// change hash
		Mockito.stub(nodes.get(0).getMd5()).toReturn("newhash".getBytes());

		// store and retrieve
		dao.persistAndReplaceFileNodes(nodes);
		resultNodes = dao.getFileNodeAttributes();

		// check content again
		checkGivenAndPersisted(resultNodes, nodes);

	}

	/**
	 * @param persistedNodes
	 * @param givenNodes
	 */
	private void checkGivenAndPersisted(List<FileNodeAttr> persistedNodes, List<FileNode> givenNodes) {
		// sort the arrays by path in order to compare the lists
		Collections.sort(givenNodes, new Comparator<FileNode>() {
			@Override
			public int compare(FileNode o1, FileNode o2) {
				return o1.getFile().toPath().compareTo(o2.getFile().toPath());
			}
		});

		Collections.sort(persistedNodes, new Comparator<FileNodeAttr>() {
			@Override
			public int compare(FileNodeAttr o1, FileNodeAttr o2) {
				return o1.getPath().compareTo(o2.getPath());
			}
		});


		// compare retrieved and given information
		assertTrue(persistedNodes.size() == givenNodes.size());
		for(int i = 0; i < persistedNodes.size(); ++i) {
			FileNodeAttr attr = persistedNodes.get(i);
			FileNode node = givenNodes.get(i);
			assertEquals(attr.getPath(), node.getFile().toPath());
			assertEquals(attr.isFile(), node.isFile());
			if (attr.isFile()) {
				assertEquals(attr.getContentHash(), PathUtils.base64Encode(node.getMd5()));
			} else {
				assertEquals("", attr.getContentHash());
			}
		}
	}

	private List<FileNode> createNodeList(int numNodes) {
		List<FileNode> nodes = new ArrayList<>();

		// create some file nodes
		for(int i = 0; i < numNodes; ++i) {
			Path file = basePath.resolve(RandomStringUtils.randomAlphabetic(12));
			boolean isFile = i % 2 == 0;
			byte[] hash = null;
			if (isFile) {
				hash = String.format("hash%d", i).getBytes();
			}

			FileNode node = createFileNode(file, isFile, hash);
			nodes.add(node);
		}

		return nodes;
	}

	private FileNode createFileNode(Path file, boolean isFile, byte[] contentHash) {
		FileNode node = Mockito.mock(FileNode.class);
		Mockito.stub(node.getFile()).toReturn(file.toFile());
		Mockito.stub(node.isFile()).toReturn(isFile);
		Mockito.stub(node.getMd5()).toReturn(contentHash);
		return node;
	}

}

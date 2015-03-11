package org.peerbox.watchservice.filetree;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.app.ClientContext;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.peerbox.watchservice.filetree.persistency.FileDao;
import org.peerbox.watchservice.states.EstablishedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTreeInitializer {

	private static final Logger logger = LoggerFactory.getLogger(FileTreeInitializer.class);

	private ClientContext context;

	public FileTreeInitializer(ClientContext context) {
		this.context = context;
	}

	/**
	 * Creates a tree consisting of the user profile (files) in the DHT combined with the files on disk.
	 *
	 * @throws Exception
	 */
	public void initialize(Path topLevel) throws Exception {
		// root of tree
		final IFileTree tree = context.getFileEventManager().getFileTree();

		// add the files in the DHT to the tree
		FileNode root = context.getFileManager().listFiles().execute();
		List<FileNode> fileList = FileNode.getNodeList(root, true, true);

		fileList = fileList.stream().filter(node -> node.getFile().toPath().
				startsWith(topLevel)).sorted(new Comparator<FileNode>() {
					@Override
					public int compare(FileNode o1, FileNode o2) {
						return o1.getFile().compareTo(o2.getFile());
					}
				}).collect(Collectors.toList());

		fileList.forEach(node -> logger.trace("fileList: {}", node.getFile().getPath()));


		for (FileNode node : fileList) {
			if(node == root) {
				continue; // root does not need to be added.
			}
			FileComponent component = null;
			if (node.isFile()) {
				component = new FileLeaf(node.getFile().toPath(), true);
			} else {
				component = new FolderComposite(node.getFile().toPath(), true);
			}
			tree.putFile(node.getFile().toPath(), component);

			component.setIsUploaded(true);
			component.getAction().setFileEventManager(context.getFileEventManager());
			component.getAction().setFile(component);
			// state: InitialState for all
		}

		// Add the files on disk to the tree (if not present yet)
		Files.walkFileTree(topLevel, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				FileComponent component = tree.getFile(dir);
				if (component == null) {
					component = new FolderComposite(dir, true);
					component.setIsUploaded(false);
					tree.putFile(dir, component);
					// state: initial, because not present in network
				} else {
					// already exists, i.e. present in network - state: established
					component.getAction().setCurrentState(new EstablishedState(component.getAction()));
				}

				component.getAction().setFileEventManager(context.getFileEventManager());
				component.getAction().setFile(component);

				return super.preVisitDirectory(dir, attrs);
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				FileComponent component = tree.getFile(file);
				if (component == null) {
					component = new FileLeaf(file, true);
					component.setIsUploaded(false);
					tree.putFile(file, component);
					// state: initial, because not present in network
				} else {
					// already exists, state: established
					component.getAction().setCurrentState(new EstablishedState(component.getAction()));
				}

				component.getAction().setFileEventManager(context.getFileEventManager());
				component.getAction().setFile(component);

				return super.visitFile(file, attrs);
			}
		});

		// selective sync: use database to disable sync on some elements
		final FileDao fileDao = context.getFileDao();

		List<FileComponent> subtreeList = tree.asList().stream().filter(node -> node.getPath().
				startsWith(topLevel)).sorted(new Comparator<FileComponent>() {
					@Override
					public int compare(FileComponent o1, FileComponent o2) {
						return o1.getPath().compareTo(o2.getPath());
					}
				}).collect(Collectors.toList());


		for (FileComponent c : subtreeList) {
			Boolean isSync = fileDao.isSynchronizedByPath(c.getPath());
			if (isSync != null && !isSync.booleanValue()) {
				c.setIsSynchronized(false);
			} else {
				if (c.getParent() != null && !c.getParent().isSynchronized()) {
					c.setIsSynchronized(false);
				} else {
					c.setIsSynchronized(true);
				}
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Tree initialization finished. Current tree state: ");
			for (FileComponent c : subtreeList) {
				logger.trace("... {}", c.toString());
				logger.trace("...... {}", c.getAction().toString());
			}
		}
	}

}

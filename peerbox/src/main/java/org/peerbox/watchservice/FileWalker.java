package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class can be used to traverse folders. This is done for two purposes:
 * The first purpose is to compute a hash which represents the structure of the subtree
 * to detect folder moves without the need of hashing over all of its (possibly huge)
 * content. The second purpose is to artificially generate local create events. This is
 * important since new folders have to be registered to the WatchService. Until this is done,
 * create events can be lost. This has to be circumvented.
 * @author Claudio
 *
 */
public class FileWalker {

	private static final Logger logger = LoggerFactory.getLogger(FileWalker.class);

	private Path rootFolder;
	private FileEventManager eventManager;

	/**
	 * This object is used to build the subtree structuredefined by the Path 
	 * {@link rootFolder}.
	 */
	private FolderComposite fileTree;

	public FileWalker(Path rootDirectory, FileEventManager eventManager){
		this.rootFolder = rootDirectory;
		this.fileTree = new FolderComposite(rootDirectory, true);
		this.eventManager = eventManager;
	}

	/**
	 * This method discovers the structure of the subtree defined by
	 * {@link rootFolder}. The structure is defined as a recursive hash
	 * over the {@link Path}s of all contained files and folders.
	 * 
	 * @return a hash representing the subtree's recursive structure.
	 */
	public String computeStructureHashOfFolder(){
		try {
			Files.walkFileTree(rootFolder, new FileIndexer(false));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileTree.getStructureHash();
	}

	/**
	 * This method discovers the subtree defined by
	 * {@link rootFolder} and throws local create events, which
	 * are forwarded to the core using the {@link org.peerbox.
	 * watchservice.FileEventManager FileEventManager}.
	 * 
	 * @return a hash representing the subtree's recursive structure.
	 */
	public void generateLocalCreateEvents() {
		try {
			Files.walkFileTree(rootFolder, new FileIndexer(true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class FileIndexer extends SimpleFileVisitor<Path> {

		private boolean throwCreates = false;

		public FileIndexer(boolean throwCreates){
			this.throwCreates = throwCreates;
		}
		@Override
		public FileVisitResult postVisitDirectory(Path path, IOException ex) throws IOException {
			return super.postVisitDirectory(path, ex);
		}

		@Override
		public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attr)
				throws IOException {
			if(throwCreates){
				if(path.toString().equals(rootFolder.toString())){
					logger.trace("Skipping root directory.");
				} else {
					eventManager.onLocalFileCreated(path);
				}
			}
			return super.preVisitDirectory(path, attr);
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attr) throws IOException {
			if(PathUtils.isFileHidden(path)){
				return FileVisitResult.CONTINUE;
			}
			if (throwCreates) {
				eventManager.onLocalFileCreated(path);
			} else {
				if (Files.isDirectory(path)) {
					FolderComposite newFolder = new FolderComposite(path, false);
					newFolder.setIsSynchronized(true);
					fileTree.putComponent(path, newFolder);
				} else {
					FileLeaf newFile = new FileLeaf(path, false);
					newFile.setIsSynchronized(true);
					fileTree.putComponent(path, newFile);
				}
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path path, IOException ex) throws IOException {
			return super.visitFileFailed(path, ex);
		}
	}
}

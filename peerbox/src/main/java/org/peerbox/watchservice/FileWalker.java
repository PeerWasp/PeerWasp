package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWalker extends AbstractWatchService {
	
	private static final Logger logger = LoggerFactory.getLogger(FileWalker.class);
	
	private Path rootDirectory;
	private FileEventManager eventManager;
	private Map<Path, Action> filesystemView = new HashMap<Path, Action>();
	private FolderComposite fileTree;
	private boolean computeContentHash = false;
	
	public FileWalker(Path rootDirectory, FileEventManager eventManager){
		this.rootDirectory = rootDirectory;
		this.fileTree = new FolderComposite(rootDirectory, true);
		this.eventManager = eventManager;
	}
	
	@Override
	public void start() throws Exception {
		
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
	}

	public void indexNamesRecursively(){
		try {
			filesystemView = new HashMap<Path, Action>();
			Files.walkFileTree(rootDirectory, new FileIndexer(false));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void alignActionMaps(){
//		indexDirectoryRecursively();
//		
//		Map<Path, Action> filePathToAction = eventManager.getFilePathToAction();
//		
//		//Set<Path> filePathToActionKeys = filePathToAction.keySet();
//		for(Entry<Path, Action> entry : filesystemView.entrySet()){
//			Path key = entry.getKey();
//			Action action = filePathToAction.get(key);
//			if (action != null){
//				if(!action.getContentHash().equals(entry.getValue().getContentHash())){
//					eventManager.onFileModified(key);
//				}
//			} else {
//				eventManager.onFileCreated(key, false);
//			}
//		}
//		for(Path p : filePathToAction.keySet()){
//			if(!filesystemView.containsKey(p)){
//				eventManager.onFileDeleted(p);
//			}
//		}
	}
	
	public String getContentNamesHashOfWalkedFolder(){
		return fileTree.getContentNamesHash();
		/*
		 * Create new Map<String, FileComponent> in which deleted components are saved with
		 * their contentNamesHash as key. if found -> move event!
		 */
		//if(eventManager.getDeletedFileComponents().)$
		
	}

	public FolderComposite indexContentRecursively() {
		computeContentHash = true;
		try {
			filesystemView = new HashMap<Path, Action>();
			Files.walkFileTree(rootDirectory, new FileIndexer(true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		computeContentHash = false;
		return fileTree;
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
				if(path.toString().equals(rootDirectory.toString())){
					logger.trace("Skipping root directory.");
				} else {
					logger.trace("create event for {} ", path);
					eventManager.onLocalFileCreated(path);
				}
			}
			return super.preVisitDirectory(path, attr);
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attr) throws IOException {
			filesystemView.put(path, new Action(eventManager));
//			fileTree.putComponent(path, ne)
			logger.debug("Found file {}", path);
			if(throwCreates){
				eventManager.onLocalFileCreated(path);
			} else {
				String oldstr = fileTree.getStructureHash();
				if(path.toFile().isDirectory()){
					fileTree.putComponent(path.toString(), new FolderComposite(path, false));
				} else {
					fileTree.putComponent(path.toString(), new FileLeaf(path, false));
				}
				logger.debug("updated structure hash: of {} from {} to {}", fileTree.getPath(), oldstr, fileTree.getStructureHash());
			}
			
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path path, IOException ex) throws IOException {
			return super.visitFileFailed(path, ex);
		}
	}
}

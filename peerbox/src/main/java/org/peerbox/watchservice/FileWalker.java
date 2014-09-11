package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FileWalker {
	
	private Path rootDirectory;
	private FileIndexer indexer;
	private FileEventManager eventManager;
	private Map<Path, Action> filesystemView = new HashMap<Path, Action>();
	
	public FileWalker(Path rootDirectory, FileEventManager eventManager){
		this.rootDirectory = rootDirectory;
		this.indexer = new FileIndexer();
		this.eventManager = eventManager;
	}
	
	public void indexDirectoryRecursively(){
		try {
			filesystemView = new HashMap<Path, Action>();
			Files.walkFileTree(rootDirectory, indexer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void alignActionMaps(){
		indexDirectoryRecursively();
		
		Map<Path, Action> filePathToAction = eventManager.getFilePathToAction();
		//Set<Path> filePathToActionKeys = filePathToAction.keySet();
		for(Entry<Path, Action> entry : filesystemView.entrySet()){
			Path key = entry.getKey();
			Action action = filePathToAction.get(key);
			if (action != null){
				if(!action.getContentHash().equals(entry.getValue().getContentHash())){
					eventManager.onFileModified(key);
				}
			} else {
				eventManager.onFileCreated(key);
			}
		}
		for(Path p : filePathToAction.keySet()){
			if(!filesystemView.containsKey(p)){
				eventManager.onFileDeleted(p);
			}
		}
	}
	
	private class FileIndexer extends SimpleFileVisitor<Path>{

		
		

		@Override
		public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
			// TODO Auto-generated method stub
			return super.postVisitDirectory(arg0, arg1);
		}

		@Override
		public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1)
				throws IOException {
			// TODO Auto-generated method stub
			return super.preVisitDirectory(arg0, arg1);
		}

		@Override
		public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {
			filesystemView.put(arg0, new Action(arg0));
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path arg0, IOException arg1) throws IOException {
			// TODO Auto-generated method stub
			return super.visitFileFailed(arg0, arg1);
		}

		
	}
}

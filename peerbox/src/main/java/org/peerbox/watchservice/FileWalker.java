package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileWalker {
	
	private Path rootDirectory;
	private FileIndexer indexer;
	private FileEventManager eventManager;
	
	public FileWalker(Path rootDirectory, FileEventManager eventManager){
		this.rootDirectory = rootDirectory;
		this.indexer = new FileIndexer();
		this.eventManager = eventManager;
	}
	
	public void indexDirectoryRecursively(){
		try {
			Files.walkFileTree(rootDirectory, indexer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class FileIndexer implements FileVisitor{

		@Override
		public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
			// TODO Auto-generated method stub
			eventManager.onFileCreated((Path)file);
			return null;
		}

		@Override
		public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}

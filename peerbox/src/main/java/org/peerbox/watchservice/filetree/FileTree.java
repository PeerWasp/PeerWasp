package org.peerbox.watchservice.filetree;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import org.peerbox.watchservice.Action;

public class FileTree {

	private FileTreeNode root;
	
	public FileTree(){
		this.root = new FileTreeNode();
	}
	
	public void putElement(Path path, Action action){
		root.put(path.toString(), action);
	}
	
	public boolean deleteElement(Path path){
		return root.delete(path.toString());
	}
	
	public FileTreeNode getElement(Path path){
		return root.get(path.toString());
	}
	
	public void printElements(){
		root.print();
	}
	

}

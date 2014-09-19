package org.peerbox.watchservice;

import java.nio.file.Path;

public class FileLeaf implements FileComponent{
	private Action action;
	private Path path;
	private String contentHash;
	private FolderComposite parent;
	
	public FileLeaf(Path path){
		this.path = path;
		this.action = new Action(path);
	}

	@Override
	public Action getAction() {
		return this.action;
	}

	@Override
	public void putComponent(String path, FileComponent component) {
		// TODO Auto-generated method stub
		System.err.println("put on file not defined.");
	}

	@Override
	public FileComponent deleteComponent(String path) {
		System.err.println("delete on file not defined");
		return null;
	}

	@Override
	public FileComponent getComponent(String path) {
		System.err.println("get  on file not defined");
		return null;
	}
	
	@Override
	public String getContentHash() {
		return contentHash;
	}

	@Override
	public void bubbleContentHashUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(FolderComposite parent) {
		this.parent = parent;
	}
}

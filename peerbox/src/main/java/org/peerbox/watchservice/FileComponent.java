package org.peerbox.watchservice;

import java.nio.file.Path;

public interface FileComponent {
	
	public String getContentHash();
	
	public Action getAction();
	
	public void putComponent(String path, FileComponent component);
	
	public FileComponent deleteComponent(String path);
	
	public FileComponent getComponent(String path);
	
}

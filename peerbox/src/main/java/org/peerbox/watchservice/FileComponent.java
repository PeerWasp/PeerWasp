package org.peerbox.watchservice;

public interface FileComponent {
	
	public String getContentHash();

	public Action getAction();
	
	public void setParent(FolderComposite parent);
	
	public void bubbleContentHashUpdate();
	
	public void putComponent(String path, FileComponent component);
	
	public FileComponent deleteComponent(String path);
	
	public FileComponent getComponent(String path);
}

package org.peerbox.watchservice;

import java.nio.file.Path;

public interface FileComponent {
	
	
	public boolean isFolder();
	public boolean isFile();
	
	public String getContentHash();

	public Action getAction();
	
	public void setParent(FolderComposite parent);
	public FolderComposite getParent();
	
	/**
	 * This function should propagate a content hash update to the parent FolderComposite
	 * in which this FileComponent is contained.
	 */
	public default void bubbleContentHashUpdate(){
		boolean hasChanged = updateContentHash();
		if(hasChanged){
			getParent().bubbleContentHashUpdate();
		}
	}
	
	public void putComponent(String path, FileComponent component);
	
	public FileComponent deleteComponent(String path);
	
	public FileComponent getComponent(String path);
	
	public boolean updateContentHash();
	
	public Path getPath();
	public void setPath(Path parent);
	

	public boolean getIsUploaded();
	public void setIsUploaded(boolean isUploaded);

}

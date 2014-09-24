package org.peerbox.watchservice;

import java.io.Serializable;
import java.nio.file.Path;

public interface FileComponent extends Serializable {
	
	public String getContentHash();

	public Action getAction();
	
	public void setParent(FolderComposite parent);
	public FolderComposite getParent();
	
	/**
	 * This function should propagate a content hash update to the parent FolderComposite
	 * in which this FileComponent is contained.
	 */
	public default void bubbleContentHashUpdate(){
		boolean hasChanged = computeContentHash();
		if(hasChanged){
			getParent().bubbleContentHashUpdate();
		}
	}
	
	public void putComponent(String path, FileComponent component);
	
	public FileComponent deleteComponent(String path);
	
	public FileComponent getComponent(String path);
	
	public boolean computeContentHash();
	
	public Path getPath();

	public boolean getIsUploaded();
	public void setIsUploaded(boolean isUploaded);

	
}

package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface FileComponent {
	
	
	public boolean isFolder();
	public boolean isFile();
	
	public String getContentHash();
	public String getStructureHash();
	public void setStructureHash(String hash);

	public IAction getAction();
	
	public void setParent(FolderComposite parent);
	public FolderComposite getParent();
	
	/**
	 * This function should propagate a content hash update to the parent FolderComposite
	 * in which this FileComponent is contained.
	 */
	public default void bubbleContentHashUpdate(){
		bubbleContentHashUpdate(null);
	}
	
	public default void bubbleContentHashUpdate(String contentHash){
		boolean hasChanged = updateContentHash(contentHash);
		if(hasChanged){
			getParent().bubbleContentHashUpdate();
		}
	}
	
	public void putComponent(String path, FileComponent component);
	
	public FileComponent deleteComponent(String path);
	
	public FileComponent getComponent(String path);
	
	public boolean updateContentHash(String contentHash);
	public boolean updateContentHash();
	public Path getPath();
	public void setParentPath(Path parent);
	public void setPath(Path path);

	public boolean getActionIsUploaded();
	public void setActionIsUploaded(boolean isUploaded);
	
	public boolean isReady();
	
	public void propagatePathChangeToChildren();

}

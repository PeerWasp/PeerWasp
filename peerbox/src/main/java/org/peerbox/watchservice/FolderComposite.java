package org.peerbox.watchservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hive2hive.core.security.EncryptionUtil;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * 
 * @author Claudio
 * Folder composite represents a file system directory.
 */
public class FolderComposite implements FileComponent{

	private SortedMap<String, FileComponent> children = new TreeMap<String, FileComponent>();
	private Action action;
	private Path path;
	private Path folderName;
	private String contentHash;
	private String contentNamesHash;
	private FolderComposite parent;
	private boolean isUploaded;
	private boolean updateContentHashes;
	
	
	public FolderComposite(Path path, boolean updateContentHashes){
		this.path = path;
		this.folderName = path.getFileName();
		this.action = new Action(path);
		this.contentHash = "";
		this.isUploaded = false;
		this.updateContentHashes = updateContentHashes;
		this.contentNamesHash = "";
		
		if(updateContentHashes){
			updateContentHash();	
		}
	
	}
	
	@Override
	public FolderComposite getParent() {
		return this.parent;
	}
	
	@Override
	public void setParent(FolderComposite parent) {
		this.parent = parent;
	}

	@Override
	public Path getPath() {
		return this.path;
	}

	@Override
	public Action getAction() {
		return this.action;
	}
	
	private Path constructFullPath(String lastPathFragment){
		String completePath = path.toString() + File.separator + lastPathFragment;
		return Paths.get(completePath);
	}

	@Override
	public String getContentHash() {
		return contentHash;
	}
	
	/**
	 * Appends a new component to the FolderComposite. Inexistent folders are added on the
	 * fly. Existing items are replaced. Triggers updates of content and name hashes.
	 */
	@Override
	public void putComponent(String remainingPath, FileComponent component) {
		
		//if the path it absolute, cut off the absolute path to the root directory!
		if(remainingPath.startsWith(path.toString())){
			remainingPath = remainingPath.substring(path.toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent;
		
		//if we are at the last recursion, perform the add, else recursively continue
		if(newRemainingPath.equals("")){
			addComponentToChildren(nextLevelPath, component);
		} else {
			nextLevelComponent = children.get(nextLevelPath);
			if(nextLevelComponent == null){
				Path completePath = constructFullPath(nextLevelPath);
				nextLevelComponent = new FolderComposite(completePath, updateContentHashes);
				addComponentToChildren(nextLevelPath, nextLevelComponent);
			}
			nextLevelComponent.putComponent(newRemainingPath, component);
		}
	} 

	/**
	 * Computes the content hash for this object by appending the content hashes of contained
	 * components and hashing over it again. 
	 * @return
	 */
	private boolean computeContentNamesHash() {
		String nameHashInput = "";
		String oldNamesHash = contentNamesHash;
		for(String childName : children.keySet()){
			nameHashInput = nameHashInput.concat(childName);
		}
		contentNamesHash = Action.createStringFromByteArray(EncryptionUtil.generateMD5Hash(nameHashInput.getBytes()));
		if(!contentNamesHash.equals(oldNamesHash)){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Deletes the FileComponent at location remainingPath. Triggers updates of 
	 * content and name hashes. 
	 * @return The deleted component. If it does not exist, null is returned
	 */
	@Override
	public FileComponent deleteComponent(String remainingPath) {
		if(remainingPath.startsWith(path.toString())){
			remainingPath = remainingPath.substring(path.toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent = children.get(nextLevelPath);
		
		if(newRemainingPath.equals("")){
			FileComponent removed = children.remove(nextLevelPath);
			if(updateContentHashes){
				bubbleContentHashUpdate();
			}
			bubbleContentNamesHashUpdate();
			return removed;
		} else {
			if(nextLevelComponent == null){
				return null;
			} else {
				FileComponent deletedComponent = nextLevelComponent.deleteComponent(newRemainingPath);
				return deletedComponent;	
			}
		}
	}
	
	/**
	 * Get the FileComponent at the specified location. Triggers updates of content and name hashes.
	 * @return If it does exist, the requested FileComponent is returned, null otherwise.
	 */
	@Override
	public FileComponent getComponent(String remainingPath){
		//if the path it absolute, cut off the absolute path to the root directory!
		if(remainingPath.startsWith(path.toString())){
			remainingPath = remainingPath.substring(path.toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent = children.get(nextLevelPath);
		
		if(newRemainingPath.equals("")){
			if(nextLevelComponent != null && updateContentHashes){
				nextLevelComponent.bubbleContentHashUpdate();
			}
			bubbleContentNamesHashUpdate();
			return children.get(nextLevelPath);
		} else {
			if(nextLevelComponent == null){
				return null;
			} else {
				return nextLevelComponent.getComponent(newRemainingPath);
			}
		}
	}

	@Override
	public boolean updateContentHash() {
		String tmp = "";
		for(FileComponent value : children.values()){
			tmp = tmp.concat(value.getContentHash());
		}
		
		byte[] rawHash = EncryptionUtil.generateMD5Hash(tmp.getBytes());
		String updatedContentHash = Base64.encode(rawHash);
		if(!contentHash.equals(updatedContentHash)){
			contentHash = Base64.encode(rawHash);
			return true;
		}
		return false;
	}
	
	@Override
	public void bubbleContentHashUpdate() {
		boolean hasChanged = updateContentHash();
		if(hasChanged && parent != null){
			parent.bubbleContentHashUpdate();
		}
	}
	
	private void bubbleContentNamesHashUpdate() {
		boolean hasChanged = computeContentNamesHash();
		if(hasChanged && parent != null){
			parent.bubbleContentNamesHashUpdate();
		}
	}

	/*
	 * Because of the new children, the content hash of the directory may change and is propagated
	 */
	private void addComponentToChildren(String nextLevelPath, FileComponent component) {
		children.remove(nextLevelPath);
		children.put(nextLevelPath, component);
		component.setParent(this);
		if(updateContentHashes){
			bubbleContentHashUpdate();			
		}
		propagatePathChangetoChildren(path);
		bubbleContentNamesHashUpdate();
	}

	@Override
	public boolean getIsUploaded() {
		return this.isUploaded;
	}
	
	@Override
	public void setIsUploaded(boolean isUploaded) {
		this.isUploaded = isUploaded;
	}
	
	public String getContentNamesHash(){
		return contentNamesHash;
	}

	
	public void setPathFragment(Path pathFragment){
		this.folderName = pathFragment;
	}
	

	/**
	 * If a subtree is appended, the children of the subtree need to update their paths.
	 * This function starts a recursive update. Furthermore, the filePath of the action
	 * related to each FileComponent is updates as well.
	 * @param parentPath
	 */
	public void propagatePathChangetoChildren(Path parentPath){
		for(FileComponent child : children.values()){
			child.setPath(parentPath);
			if(child instanceof FolderComposite){
				FolderComposite childAsFolder = (FolderComposite)child;
				childAsFolder.propagatePathChangetoChildren(getPath());
			}

		}
	}
	
	@Override
	public void setPath(Path parentPath){	
		if(parentPath != null){
			this.path = Paths.get(new File(parentPath.toString(), folderName.toString()).getPath());
			action.setPath(this.path);
		}
	}
}

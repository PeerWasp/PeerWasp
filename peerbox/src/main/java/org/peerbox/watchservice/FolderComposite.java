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
	private String contentHash;
	private FolderComposite parent;
	
	public FolderComposite(Path path){
		this.path = path;
		this.action = new Action(path);
		this.contentHash = "";
		computeContentHash();		
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
		return path;
	}

	@Override
	public Action getAction() {
		return action;
	}
	
	private Path constructFullPath(String lastPathFragment){
		String completePath = path + File.separator + lastPathFragment;
		return Paths.get(completePath);
	}

	@Override
	public String getContentHash() {
		return contentHash;
	}
	
	@Override
	public void putComponent(String remainingPath, FileComponent component) {
		
		//if the path it absolute, cut off the absolute path to the root directory!
		if(remainingPath.startsWith(path.toString())){
			remainingPath = remainingPath.substring(path.toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent;
		
		//if we are at the last recursion, perform the add
		if(newRemainingPath.equals("")){
			addComponentToChildren(nextLevelPath, component);
		} else {
			nextLevelComponent = children.get(nextLevelPath);
			
			//create missing directories on the path on the fly
			if(nextLevelComponent == null){
				Path completePath = constructFullPath(nextLevelPath);
				nextLevelComponent = new FolderComposite(completePath);
				addComponentToChildren(nextLevelPath, nextLevelComponent);
			}
			nextLevelComponent.putComponent(newRemainingPath, component);
		}
	} 

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
			bubbleContentHashUpdate();
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
			if(nextLevelComponent != null){
				nextLevelComponent.bubbleContentHashUpdate();
			}
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
	public boolean computeContentHash() {
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
		boolean hasChanged = computeContentHash();
		if(hasChanged && parent != null){
			parent.bubbleContentHashUpdate();
		}
	}
	
	/*
	 * Because of the new children, the content hash of the directory may change and is propagated
	 */
	private void addComponentToChildren(String nextLevelPath, FileComponent component) {
		children.put(nextLevelPath, component);
		component.setParent(this);
		bubbleContentHashUpdate();
	}
}

package org.peerbox.watchservice.filetree.composite;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.hive2hive.core.security.HashUtil;
import org.peerbox.watchservice.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Claudio
 * Represents a folder in the application internal FileTree.
 */
public class FolderComposite extends AbstractFileComponent{

	private static final Logger logger = LoggerFactory.getLogger(FolderComposite.class);
	
	private SortedMap<String, FileComponent> children = new ConcurrentSkipListMap<String, FileComponent>();

	private String contentNamesHash;
	private boolean updateContentHashes;
	private boolean isRoot = false;
	
	public FolderComposite(Path path, boolean updateContentHashes, boolean isRoot){
		super(path);
		
		this.updateContentHashes = updateContentHashes;
		this.contentNamesHash = "";
		this.isRoot = isRoot;
		
		if(isRoot){
			getAction().setIsUploaded(true);
			setIsSynchronized(true);
		}
		if(updateContentHashes){
			updateContentHash();	
		}
	}
	
	public FolderComposite(Path path, boolean updateContentHashes){
		this(path, updateContentHashes, false);
	}
	
	public SortedMap<String, FileComponent> getChildren(){
		return children;
	}
	
	private Path constructFullPath(String lastPathFragment){
		String completePath = getPath().toString() + File.separator + lastPathFragment;
		System.out.println("CompletePath: " + completePath);
		return Paths.get(completePath);
	}

	/**
	 * Appends a new component to the FolderComposite. Inexistent folders are added on the
	 * fly. Existing items are replaced. Triggers updates of content and name hashes.
	 */
	@Override
	public synchronized void putComponent(String remainingPath, FileComponent component) {
//		component.getAction().setPath(Paths.get(remainingPath));
		//if the path it absolute, cut off the absolute path to the root directory!
		if(remainingPath.startsWith(getPath().toString())){
			remainingPath = remainingPath.substring(getPath().toString().length() + 1);
		}
		//logger.trace("after remainingpath calculation {}", remainingPath);
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent;
		if(newRemainingPath == null){
			//logger.debug("newRemainingPath == null");
		}
		//if we are at the last recursion, perform the add, else recursively continue
		if(newRemainingPath.equals("")){
			//logger.trace("newRemainingPath is empty {}", remainingPath);
			if(nextLevelPath == null){
				//logger.trace("nextLevelPath is NULL {}", remainingPath);
			}
			//logger.trace("nextLevelPath is {}", nextLevelPath);
			addComponentToChildren(nextLevelPath, component);
			//logger.trace("after addComponentToChildren {}", nextLevelPath);
		} else {
			//logger.trace("newRemainingPath is not empty {}", remainingPath);
			nextLevelComponent = children.get(nextLevelPath);
			//logger.trace("after children.get {}", remainingPath);
			if(nextLevelComponent == null){
				//logger.trace("newLevelComponent is null {}", remainingPath);
				Path completePath = constructFullPath(nextLevelPath);
				//logger.trace("after completePathConstrution{} {}", remainingPath, completePath);
				nextLevelComponent = new FolderComposite(completePath, updateContentHashes);
				//logger.trace("after new FolderComposite( {} {}", remainingPath, completePath);
				addComponentToChildren(nextLevelPath, nextLevelComponent);
				//logger.trace("after addComponentToChildren2 {} {}", nextLevelPath, nextLevelComponent);
			}

			nextLevelComponent.putComponent(newRemainingPath, component);
		//	logger.trace("success nextLevelComponent.putComponen", remainingPath);
		}
		//logger.trace("success putComponent", nextLevelPath);
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
		contentNamesHash = PathUtils.createStringFromByteArray(HashUtil.hash(nameHashInput.getBytes()));
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
		if(remainingPath.startsWith(getPath().toString())){
			remainingPath = remainingPath.substring(getPath().toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent = children.get(nextLevelPath);

		if(newRemainingPath.equals("")){
			FileComponent removed = children.remove(nextLevelPath);
//			logger.debug("Removed {}", removed.getPath());
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
	public synchronized FileComponent getComponent(String remainingPath){
		//if the path it absolute, cut off the absolute path to the root directory!
		logger.debug("Root: {} FilePath: {}", getPath(), remainingPath);
		if(remainingPath.toString().equals(getPath().toString())){
			logger.debug("Return root");
			return this;
		}
		if(remainingPath.startsWith(getPath().toString())){
			remainingPath = remainingPath.substring(getPath().toString().length() + 1);
		}
		
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent = children.get(nextLevelPath);
//		for(Map.Entry<String, FileComponent> child : children.entrySet()){
//			logger.debug("{} has child {}", getPath(), child.getKey());
//		}
		
		if(newRemainingPath.equals("")){
//			if(nextLevelComponent != null && updateContentHashes){
//				nextLevelComponent.bubbleContentHashUpdate();
//			}
//			bubbleContentNamesHashUpdate();
			return children.get(nextLevelPath);
		} else {
			if(nextLevelComponent == null){
				return null;
			} else {
				return nextLevelComponent.getComponent(newRemainingPath);
			}
		}
	}


	public boolean updateContentHash() {
//		logger.debug("enter updateContentHash() in FolderComposite()");
		return updateContentHash(null);
	}
	
	private boolean updateContentHash(String newHash) {
//		logger.debug("enter updateContentHash(String) in FolderComposite()");
		if(newHash == null){
//			logger.debug("enter newHash == null");
			String tmp = "";
			for(FileComponent value : children.values()){
			//	logger.debug("value: {}", value.getPath());
				tmp = tmp.concat(value.getContentHash());
			}
//			logger.trace("read children");
			byte[] rawHash = HashUtil.hash(tmp.getBytes());
//			logger.trace("got bytes");
			newHash = Base64.getEncoder().encodeToString(rawHash);
//			logger.trace("got newHash");
		}
		if(!getContentHash().equals(newHash)){
			setContentHash(newHash);
//			logger.trace("successful new hash");
			return true;
		}
//		logger.trace("successful no new hash");
		return false;
	}
	
	@Override
	public void bubbleContentHashUpdate() {
		bubbleContentHashUpdate(null);
	
	}
	
	@Override
	public void bubbleContentHashUpdate(String contentHash) {
		// TODO Auto-generated method stub
		boolean hasChanged = updateContentHash();
		if(hasChanged && getParent() != null){
			getParent().bubbleContentHashUpdate();
		}

	}
	
	private void bubbleContentNamesHashUpdate() {
//		logger.debug("Structure hash of {} before: {}", path, contentNamesHash);
		boolean hasChanged = computeContentNamesHash();
//		logger.debug("Structure hash of {} after: {}", path, contentNamesHash);
//		logger.debug("successful computeContentNamesHash hasChanged {}", hasChanged);
		if(hasChanged && getParent() != null){
//			logger.debug("start partent.bubbleContentNamesHashUpdate");
			getParent().bubbleContentNamesHashUpdate();
//			logger.debug("finish partent.bubbleContentNamesHashUpdate");
		}
	}

	/*
	 * Because of the new children, the content hash of the directory may change and is propagated
	 */
	private void addComponentToChildren(String nextLevelPath, FileComponent component) {
		children.remove(nextLevelPath);
		children.put(nextLevelPath, component);
//		logger.trace("after remove/put {}", nextLevelPath);
//		if(component == null){
//			logger.trace("COMPONENT IS NULL");
//		}
		component.setParent(this);
		logger.trace("SET Parent for {} is {}", component.getPath(), getPath());
//		logger.trace("after setParent {}", nextLevelPath);
		if(updateContentHashes){
//			logger.trace("START bubbleContentHashUpdate {}", nextLevelPath);
			bubbleContentHashUpdate();		
//			logger.trace("END bubbleContentHashUpdate {}", nextLevelPath);
		}
//		logger.trace("after bubbleContentHashUpdate {}", nextLevelPath);
		component.setParentPath(getPath());
//		logger.trace("after setPath {}", nextLevelPath);
		if(component instanceof FolderComposite){
			FolderComposite componentAsFolder = (FolderComposite)component;
			componentAsFolder.propagatePathChangeToChildren();
		}
//		logger.trace("BEFORE bubbleContentNamesHashUpdate {}", nextLevelPath);
		bubbleContentNamesHashUpdate();
//		logger.trace("successful bubbleContentNamesHashUpdate {}", nextLevelPath);
	}

	@Override
	public boolean getActionIsUploaded() {
		return this.getAction().getIsUploaded();
	}
	
	@Override
	public void setActionIsUploaded(boolean isUploaded) {
		this.getAction().setIsUploaded(isUploaded);
		for(FileComponent child : children.values()){
			child.getAction().setIsUploaded(isUploaded);
		}
	}
	
	public String getContentNamesHash(){
		return contentNamesHash;
	}

	/**
	 * If a subtree is appended, the children of the subtree need to update their paths.
	 * This function starts a recursive update. Furthermore, the filePath of the action
	 * related to each FileComponent is updates as well.
	 * @param parentPath
	 */
	public void propagatePathChangeToChildren(){
//		System.out.println("getPath(): " + getPath());
		for(FileComponent child : children.values()){
			child.setParentPath(getPath());
			if(child instanceof FolderComposite){
				FolderComposite childAsFolder = (FolderComposite)child;
				childAsFolder.propagatePathChangeToChildren();
			}

		}
	}
	
	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean isReady() {
		if(isRoot){
			return true;
		} else {
			logger.trace("Parent for {} is {}", getPath(), getParent());
			if(getParent().getActionIsUploaded()){
				return true;
			}
			return false;
		}
		
		
	}

	@Override
	public String getStructureHash() {
		// TODO Auto-generated method stub
		return contentNamesHash;
	}

	@Override
	public void setStructureHash(String hash) {
		contentNamesHash = hash;
	}

	@Override
	public void setIsSynchronized(boolean isSynchronized) {
		super.setIsSynchronized(isSynchronized);
		for(FileComponent comp : children.values()){
			comp.setIsSynchronized(isSynchronized);
		}
	}

	@Override
	public void getSynchronizedChildrenPaths(Set<Path> synchronizedPaths) {
		if(getIsSynchronized()){
			logger.debug("Add {} to synchronized files.", getPath());
			synchronizedPaths.add(getPath());	
		}
		for(Map.Entry<String, FileComponent> entry : children.entrySet()){
			if(entry.getValue().getIsSynchronized()){
				logger.debug("--Add {} to synchronized files.", entry.getValue().getPath());
				synchronizedPaths.add(entry.getValue().getPath());	
				entry.getValue().getSynchronizedChildrenPaths(synchronizedPaths);
			}
		}
	}

	@Override
	public void propagateIsUploaded() {
		setActionIsUploaded(true);
		if(!isRoot){
			getParent().propagateIsUploaded();
		}
	}
}

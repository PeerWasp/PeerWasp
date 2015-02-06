package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;
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
public class FolderComposite extends AbstractFileComponent {

	private static final Logger logger = LoggerFactory.getLogger(FolderComposite.class);

	private final SortedMap<Path, FileComponent> children;

	private String structureHash;
	private boolean isRoot = false;

	public FolderComposite(final Path path, boolean updateContentHash, boolean isRoot) {
		super(path, updateContentHash);

		this.children = new ConcurrentSkipListMap<Path, FileComponent>();
		computeContentNamesHash();
		this.isRoot = isRoot;

		if (isRoot) {
			setIsUploaded(true);
			setIsSynchronized(true);
		}

		if (updateContentHash) {
			updateContentHash();
		}
	}

	public FolderComposite(final Path path, boolean updateContentHash) {
		this(path, updateContentHash, false);
	}

	@Override
	public synchronized FileComponent getComponent(Path remainingPath) {
		if (remainingPath.equals(getPath())) {
			return this;
		}
		remainingPath = stripOffPrefix(remainingPath, getPath());

		Path nextLevelPath = remainingPath.getName(0);
		FileComponent nextLevel = children.get(nextLevelPath);

		if (nextLevel == null) {
			// next level child not found
			return null;
		} else if (remainingPath.getNameCount() == 1) {
			// nextLevel is the last level of path: return it
			return nextLevel;
		} else if (nextLevel.isFolder()) {
			// go to next level if it is a folder
			Path newRemainingPath = remainingPath.subpath(1, remainingPath.getNameCount());
			return nextLevel.getComponent(newRemainingPath);
		} else {
			// not possible to recurse further.
			return null;
		}
	}

	private Path stripOffPrefix(Path path, final Path prefix) {
		if (path.startsWith(prefix)) {
			path = prefix.relativize(path);
		}
		return path;
	}

	/**
	 * Appends a new component to the FolderComposite. Inexistent folders are added on the
	 * fly. Existing items are replaced. Triggers updates of content and name hashes.
	 */
	@Override
	public synchronized void putComponent(Path remainingPath, FileComponent component) {
		remainingPath = stripOffPrefix(remainingPath, getPath());

		Path nextLevelPath = remainingPath.getName(0);

		// if we are at the last recursion, perform the add, else recursively continue
		if (remainingPath.getNameCount() == 1) {
			addComponentToChildren(nextLevelPath, component);
		} else {
			FileComponent nextLevel = children.get(nextLevelPath);
			if (nextLevel == null) {
				// next level does not exist yet, create it
				Path childPath = constructFullPath(nextLevelPath);
				nextLevel = new FolderComposite(childPath, updateContentHash);
				addComponentToChildren(nextLevelPath, nextLevel);
			}
			Path newRemainingPath = remainingPath.subpath(1, remainingPath.getNameCount());
			nextLevel.putComponent(newRemainingPath, component);
		}
	}

	/*
	 * Because of the new children, the content hash of the directory may change and is propagated
	 */
	private void addComponentToChildren(final Path nextLevelPath, final FileComponent component) {
		children.remove(nextLevelPath);
		children.put(nextLevelPath, component);
		component.setParent(this);
		if (updateContentHash) {
			bubbleContentHashUpdate();
		}
		updateParentPathInChild(component);

		if (component.isFolder()) {
			FolderComposite componentAsFolder = (FolderComposite) component;
			componentAsFolder.propagatePathChangeToChildren();
		}
		bubbleContentNamesHashUpdate();
	}

	private Path updateParentPathInChild(final FileComponent child) {
		Path childName = child.getPath().getFileName();
		Path newChildPath = getPath().resolve(childName);
		child.setPath(newChildPath);
		return newChildPath;
	}

	/**
	 * Deletes the FileComponent at location remainingPath. Triggers updates of
	 * content and name hashes.
	 *
	 * @return The deleted component. If it does not exist, null is returned
	 */
	@Override
	public FileComponent deleteComponent(Path remainingPath) {
		remainingPath = stripOffPrefix(remainingPath, getPath());

		Path nextLevelPath = remainingPath.getName(0);

		FileComponent removed = null;
		if (remainingPath.getNameCount() == 1) {
			removed = children.remove(nextLevelPath);
			if (updateContentHash) {
				bubbleContentHashUpdate();
			}
			bubbleContentNamesHashUpdate();
		} else {
			FileComponent nextLevel = children.get(nextLevelPath);
			if (nextLevel != null && nextLevel.isFolder()) {
				Path newRemainingPath = remainingPath.subpath(1, remainingPath.getNameCount());
				removed = nextLevel.deleteComponent(newRemainingPath);
			}
		}
		return removed;
	}

	private Path constructFullPath(final Path name) {
		Path completePath = getPath().resolve(name);
		return completePath;
	}

	/**
	 * Computes the content hash for this object by appending the content hashes of contained
	 * components and hashing over it again.
	 *
	 * @return
	 */
	private boolean computeContentNamesHash() {
		String nameHashInput = "";
		String oldNamesHash = structureHash;
		logger.trace("Before: structure hash of {} is {}", getPath(), oldNamesHash);
		for (Map.Entry<Path, FileComponent> child : children.entrySet()) {
			if(child.getValue().isSynchronized()){
//				logger.trace("Extend names hash of {} using {}", getParent(), child.getKey());
				nameHashInput = nameHashInput.concat(child.getKey().toString());
			}
			
		}

		byte[] rawHash = HashUtil.hash(nameHashInput.getBytes());
		structureHash = PathUtils.createStringFromByteArray(rawHash);
		logger.trace("After: structure hash of {} is {}", getPath(), structureHash);
		boolean hasChanged = !structureHash.equals(oldNamesHash);
		return hasChanged;
	}

	public void bubbleContentNamesHashUpdate() {
		boolean hasChanged = computeContentNamesHash();
		if (hasChanged && getParent() != null) {
			getParent().bubbleContentNamesHashUpdate();
		}
	}

	@Override
	protected boolean updateContentHash() {
		String hashOfChildren = "";
		for (FileComponent child : children.values()) {
			if(child.isSynchronized()){
//				logger.trace("Extend content hash with {}", child.getPath());
				hashOfChildren = hashOfChildren.concat(child.getContentHash());
			}
		}

		byte[] rawHash = HashUtil.hash(hashOfChildren.getBytes());
		String newHash = PathUtils.createStringFromByteArray(rawHash);

		if (!getContentHash().equals(newHash)) {
			setContentHash(newHash);
			logger.trace("Content hash is {}", newHash);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If a subtree is appended, the children of the subtree need to update their paths.
	 * This function starts a recursive update. Furthermore, the filePath of the action
	 * related to each FileComponent is updates as well.
	 *
	 * @param parentPath
	 */
	private void propagatePathChangeToChildren() {
		for (FileComponent child : children.values()) {
			updateParentPathInChild(child);
			if (child.isFolder()) {
				FolderComposite childAsFolder = (FolderComposite) child;
				childAsFolder.propagatePathChangeToChildren();
			}
		}
	}

	@Override
	public void getSynchronizedChildrenPaths(Set<Path> synchronizedPaths) {
		if (isSynchronized()) {
			logger.debug("Add {} to synchronized files.", getPath());
			synchronizedPaths.add(getPath());
		}
		for (Map.Entry<Path, FileComponent> entry : children.entrySet()) {
			if (entry.getValue().isSynchronized()) {
				logger.debug("--Add {} to synchronized files.", entry.getValue().getPath());
				synchronizedPaths.add(entry.getValue().getPath());
				entry.getValue().getSynchronizedChildrenPaths(synchronizedPaths);
			}
		}
	}

	@Override
	public String getStructureHash() {
		return structureHash;
	}

	@Override
	public void setStructureHash(String structureHash) {
		this.structureHash = structureHash;
	}

	@Override
	public void setIsSynchronized(boolean isSynchronized) {
		super.setIsSynchronized(isSynchronized);
		for (FileComponent child : children.values()) {
			child.setIsSynchronized(isSynchronized);
		}
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean isReady() {
		if (isRoot) {
			return true;
		} else {
			boolean parentIsUploaded = getParent().isUploaded();
			return parentIsUploaded;
		}
	}

	public SortedMap<Path, FileComponent> getChildren() {
		return children;
	}
}

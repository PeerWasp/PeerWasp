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

	private final SortedMap<String, FileComponent> children;

	private String structureHash;
	private boolean isRoot = false;

	public FolderComposite(final Path path, boolean updateContentHash, boolean isRoot) {
		super(path, updateContentHash);

		this.children = new ConcurrentSkipListMap<String, FileComponent>();
		this.structureHash = "";
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
	public synchronized FileComponent getComponent(String remainingPath) {
		final String pathStr = getPath().toString();
		if (remainingPath.equals(pathStr)) {
			return this;
		}
		remainingPath = stripOffPrefix(remainingPath, pathStr);

		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);

		FileComponent nextLevel = children.get(nextLevelPath);
		if (newRemainingPath.equals("") || nextLevel == null) {
			return nextLevel;
		} else {
			if (nextLevel.isFolder()) {
				return nextLevel.getComponent(newRemainingPath);
			} else {
				return null;
			}
		}
	}

	private String stripOffPrefix(String str, final String prefix) {
		if (str.startsWith(prefix)) {
			str = str.substring(prefix.length() + 1);
		}
		return str;
	}

	/**
	 * Appends a new component to the FolderComposite. Inexistent folders are added on the
	 * fly. Existing items are replaced. Triggers updates of content and name hashes.
	 */
	@Override
	public synchronized void putComponent(String remainingPath, FileComponent component) {
		final String pathStr = getPath().toString();
		remainingPath = stripOffPrefix(remainingPath, pathStr);

		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);

		// if we are at the last recursion, perform the add, else recursively continue
		if (newRemainingPath.equals("")) {
			addComponentToChildren(nextLevelPath, component);
		} else {
			FileComponent nextLevel = children.get(nextLevelPath);
			if (nextLevel == null) {
				Path childPath = constructFullPath(nextLevelPath);
				nextLevel = new FolderComposite(childPath, updateContentHash);
				addComponentToChildren(nextLevelPath, nextLevel);
			}
			nextLevel.putComponent(newRemainingPath, component);
		}
	}

	/*
	 * Because of the new children, the content hash of the directory may change and is propagated
	 */
	private void addComponentToChildren(final String nextLevelPath, final FileComponent component) {
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
	public FileComponent deleteComponent(String remainingPath) {
		final String pathStr = getPath().toString();
		remainingPath = stripOffPrefix(remainingPath, pathStr);

		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);

		FileComponent removed = null;
		if (newRemainingPath.equals("")) {
			removed = children.remove(nextLevelPath);
			if (updateContentHash) {
				bubbleContentHashUpdate();
			}
			bubbleContentNamesHashUpdate();
		} else {
			FileComponent nextLevel = children.get(nextLevelPath);
			if (nextLevel != null) {
				removed = nextLevel.deleteComponent(newRemainingPath);
			}
		}
		return removed;
	}

	private Path constructFullPath(final String name) {
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
		for (String childName : children.keySet()) {
			nameHashInput = nameHashInput.concat(childName);
		}

		byte[] rawHash = HashUtil.hash(nameHashInput.getBytes());
		structureHash = PathUtils.createStringFromByteArray(rawHash);
		boolean hasChanged = !structureHash.equals(oldNamesHash);
		return hasChanged;
	}

	private void bubbleContentNamesHashUpdate() {
		boolean hasChanged = computeContentNamesHash();
		if (hasChanged && getParent() != null) {
			getParent().bubbleContentNamesHashUpdate();
		}
	}

	@Override
	protected boolean updateContentHash() {
		String hashOfChildren = "";
		for (FileComponent child : children.values()) {
			hashOfChildren = hashOfChildren.concat(child.getContentHash());
		}

		byte[] rawHash = HashUtil.hash(hashOfChildren.getBytes());
		String newHash = PathUtils.createStringFromByteArray(rawHash);

		if (!getContentHash().equals(newHash)) {
			setContentHash(newHash);
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
		for (Map.Entry<String, FileComponent> entry : children.entrySet()) {
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

	public SortedMap<String, FileComponent> getChildren() {
		return children;
	}
}

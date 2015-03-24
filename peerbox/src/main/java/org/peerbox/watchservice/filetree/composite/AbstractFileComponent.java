package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;

import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is used to extract commonalities between files
 * and folder in the {@link org.peerbox.watchservice.filetree.FileTree
 * FileTree}
 * @author Claudio
 */
public abstract class AbstractFileComponent implements FileComponent {
	private static final Logger logger = LoggerFactory.getLogger(AbstractFileComponent.class);

	/**
	 * Used to maintain the state of the file and handle incoming
	 * file events.
	 */
	private final IAction action;
	private Path path;

	/**
	 * A hash representing the objects content. This is defined for folders too.
	 */
	private String contentHash;

	/**
	 * True if the object is synchronized (downloaded and existing in the file-system)
	 * to the local machine.
	 */
	private boolean isSynchronized;

	/**
	 * True if the object was uploaded to the network. Hence, this is false for
	 * newly created objects.
	 */
	private boolean isUploaded;

	/**
	 * True if content hash changes should be propagated to ascending nodes in
	 * the {@link org.peerbox.watchservice.filetree.FileTree FileTree}. This is
	 * important to keep the recursive hashes up-to-date, to see if a part of
	 * the tree changed between two queries.
	 */
	protected final boolean updateContentHash;

	private FolderComposite parent;

	protected AbstractFileComponent(final Path path, final boolean updateContentHash) {
		this.action = new Action();
		this.path = path;
		this.contentHash = "";
		this.updateContentHash = updateContentHash;
		this.isUploaded = false;
		this.isSynchronized = false;
	}

	/**
	 * @return The {@link #action}.
	 */
	@Override
	public final IAction getAction() {
		return action;
	}

	/**
	 * @return The flag {@link #isUploaded}.
	 */
	@Override
	public final boolean isUploaded() {
		logger.trace("isUploaded of {} with ID {} is {}", getPath(), hashCode(), isUploaded);
		return isUploaded;
	}

	/**
	 * @return Sets the flag {@link #isUploaded}.
	 */
	@Override
	public final void setIsUploaded(boolean isUploaded) {
		logger.trace("SetIsUploaded of {}  with ID {} to {}", getPath(), hashCode(), isUploaded);
		this.isUploaded = isUploaded;
	}

	@Override
	public final Path getPath() {
		return this.path;
	}

	@Override
	public final void setPath(Path path) {
		this.path = path;
	}

	/**
	 * @return The parent folder ({@link org.peerbox.watchservice.filetree.composite.
	 * FolderComposite FolderComposite}) of this object.
	 */
	@Override
	public final FolderComposite getParent() {
		return parent;
	}

	/**
	 * Sets the parent folder ({@link org.peerbox.watchservice.filetree.composite.
	 * FolderComposite FolderComposite}) of this object.
	 */
	@Override
	public final void setParent(final FolderComposite parent) {
		logger.trace("Set parent of {} to {}", getPath(), parent.getPath());
		this.parent = parent;
	}

	/**
	 * @return The {@link #contentHash} as a String.
	 */
	@Override
	public final String getContentHash() {
		return contentHash;
	}

	/**
	 * Sets the {@link #contentHash}. This is only public because it
	 * has to be used in the {@link org.peerbox.watchservice.filetree.
	 * persistency.LocalFileDao FileDao} class. Be aware that this method
	 * should not be used to set the {@link #contentHash} to arbitrary
	 * values.
	 */
	protected final void setContentHash(final String contentHash) {
		this.contentHash = contentHash;
	}

	/**
	 * Updates the {@link #contentHash} recursively, i.e. first on
	 * the object it is called, and then on ascending {@link org.
	 * peerbox.watchservice.filetree.composite.FolderComposite
	 * FolderComposite}s in case it changed. If there was no change,
	 * the handling concludes silently.
	 *
	 * @return True if the {@link #contentHash} changed.
	 */
	@Override
	public final boolean updateContentHash() {
		boolean hasChanged = computeContentHash();
		if (hasChanged && getParent() != null) {
			getParent().updateContentHash();
		}
		return hasChanged;
	}

	protected abstract boolean computeContentHash();

	@Override
	public final boolean isSynchronized() {
		return isSynchronized;
	}

	@Override
	public void setIsSynchronized(boolean isSynchronized) {
//		logger.trace("setIsSynchronized of {} with ID {} to {}", getPath(), hashCode(), isSynchronized);
		this.isSynchronized = isSynchronized;
	}

	/**
	 * @return True if it is a folder {@link org.peerbox.watchservice.filetree.
	 * composite.FolderComposite FolderComposite)
	 */
	@Override
	public final boolean isFolder() {
		return !isFile();
	}

//	@Override
//	public String getStructureHash() {
//		String msg = String.format("getStructureHash not implemented. "
//				+ "This is probably a file. "
//				+ "(this=%s)", getPath());
//
//		throw new NotImplementedException(msg);
//	}
//
//	@Override
//	public void setStructureHash(String hash) {
//		String msg = String.format("setStructureHash not implemented. "
//				+ "This is probably a file. "
//				+ "(this=%s, hash=%s)", getPath(), hash);
//
//		throw new NotImplementedException(msg);
//	}

//	@Override
//	public void getSynchronizedChildrenPaths(Set<Path> synchronizedPaths) {
//		String msg = String.format("getSynchronizedChildrenPaths not implemented. "
//				+ "This is probably a file. "
//						+ "(this=%s)", getPath());
//
////		throw new NotImplException(msg);
//	}

	@Override
	public abstract String toString();

}

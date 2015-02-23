package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;
import java.util.Set;

import org.peerbox.exceptions.NotImplException;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractFileComponent implements FileComponent {
	private static final Logger logger = LoggerFactory.getLogger(AbstractFileComponent.class);
	private long id;

	private final IAction action;
	private Path path;
	private String contentHash;
	private boolean isSynchronized;
	private boolean isUploaded;
	protected final boolean updateContentHash;

	private FolderComposite parent;

	protected AbstractFileComponent(final Path path, final boolean updateContentHash) {
		this.id = Long.MIN_VALUE;
		this.action = new Action();
		this.path = path;
		this.contentHash = "";
		this.updateContentHash = updateContentHash;
		this.isUploaded = false;
		this.isSynchronized = false;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public final IAction getAction() {
		return action;
	}

	@Override
	public final boolean isUploaded() {
		return isUploaded;
	}

	@Override
	public final void setIsUploaded(boolean isUploaded) {
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

	@Override
	public final FolderComposite getParent() {
		return parent;
	}

	@Override
	public final void setParent(final FolderComposite parent) {
		this.parent = parent;
	}

	@Override
	public final String getContentHash() {
		return contentHash;
	}

	@Override
	public final void setContentHash(final String contentHash) {
		this.contentHash = contentHash;
	}

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
		logger.trace("setIsSynchronized of {} with ID {} to {}", getPath(), hashCode(), isSynchronized);
		this.isSynchronized = isSynchronized;
	}

	@Override
	public final boolean isFolder() {
		return !isFile();
	}

	@Override
	public void putComponent(Path path, FileComponent component) {
		String msg = String.format("putComponent not implemented. "
				+ "This is probably a file. "
				+ "(this=%s, parameter=%s)", getPath(), path);

		throw new NotImplException(msg);
	}

	@Override
	public FileComponent deleteComponent(Path path) {
		String msg = String.format("deleteComponent not implemented. "
				+ "This is probably a file. "
				+ "(this=%s, parameter=%s)", getPath(), path);

		throw new NotImplException(msg);
	}

	@Override
	public FileComponent getComponent(Path path) {
		String msg = String.format("getComponent not implemented. "
				+ "This is probably a file. "
				+ "(this=%s, parameter=%s)", getPath(), path);

		throw new NotImplException(msg);
	}

	@Override
	public String getStructureHash() {
		String msg = String.format("getStructureHash not implemented. "
				+ "This is probably a file. "
				+ "(this=%s)", getPath());

		throw new NotImplException(msg);
	}

	@Override
	public void setStructureHash(String hash) {
		String msg = String.format("setStructureHash not implemented. "
				+ "This is probably a file. "
				+ "(this=%s, hash=%s)", getPath(), hash);

		throw new NotImplException(msg);
	}

	@Override
	public void getSynchronizedChildrenPaths(Set<Path> synchronizedPaths) {
		String msg = String.format("getSynchronizedChildrenPaths not implemented. "
				+ "This is probably a file. "
						+ "(this=%s)", getPath());

//		throw new NotImplException(msg);
	}

	@Override
	public abstract String toString();

}

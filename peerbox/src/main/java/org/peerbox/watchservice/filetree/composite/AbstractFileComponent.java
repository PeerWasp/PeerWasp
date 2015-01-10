package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;
import java.util.Set;

import org.peerbox.exceptions.NotImplException;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.IAction;

abstract class AbstractFileComponent implements FileComponent {

	private final IAction action;
	private Path path;
	private String contentHash;
	private boolean isSynchronized;
	protected final boolean updateContentHash;

	private FolderComposite parent;

	AbstractFileComponent(final Path path, final boolean updateContentHash) {
		this.action = new Action();
		this.path = path;
		this.contentHash = "";
		this.updateContentHash = updateContentHash;
		this.isSynchronized = false;
	}

	@Override
	public IAction getAction() {
		return action;
	}

	@Override
	public boolean isActionUploaded() {
		return action.isUploaded();
	}

	@Override
	public void setIsActionUploaded(boolean isUploaded) {
		action.setIsUploaded(isUploaded);
	}

	@Override
	public Path getPath() {
		return this.path;
	}

	@Override
	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public void setParentPath(Path parentPath) {
		if (parentPath != null) {
			Path newPath = parentPath.resolve(getPath().getFileName());
			setPath(newPath);
			// logger.debug("Set path to {}", newPath);
		}
	}

	@Override
	public FolderComposite getParent() {
		return parent;
	}

	@Override
	public void setParent(FolderComposite parent) {
		this.parent = parent;
	}

	@Override
	public String getContentHash() {
		return contentHash;
	}

	protected void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}

	@Override
	public boolean isSynchronized() {
		return isSynchronized;
	}

	@Override
	public void setIsSynchronized(boolean isSynchronized) {
		this.isSynchronized = isSynchronized;
	}

	@Override
	public boolean isFolder() {
		return !isFile();
	}

	@Override
	public void putComponent(String path, FileComponent component) {
		String msg = String.format("putComponent not implemented. "
				+ "This is probably a file. "
				+ "(this=%s, parameter=%s)", getPath(), path);

		throw new NotImplException(msg);
	}

	@Override
	public FileComponent deleteComponent(String path) {
		String msg = String.format("deleteComponent not implemented. "
				+ "This is probably a file. "
				+ "(this=%s, parameter=%s)", getPath(), path);

		throw new NotImplException(msg);
	}

	@Override
	public FileComponent getComponent(String path) {
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
	public void propagatePathChangeToChildren() {
		String msg = String.format("propagatePathChangeToChildren not implemented. "
				+ "This is probably a file. "
				+ "(this=%s)", getPath());

		throw new NotImplException(msg);
	}

	@Override
	public void getSynchronizedChildrenPaths(Set<Path> synchronizedPaths) {
		String msg = String.format("getSynchronizedChildrenPaths not implemented. "
				+ "This is probably a file. "
						+ "(this=%s)", getPath());

		throw new NotImplException(msg);
	}

	@Override
	public boolean bubbleContentHashUpdate() {
		boolean hasChanged = updateContentHash();
		if (hasChanged && getParent() != null) {
			getParent().bubbleContentHashUpdate();
		}
		return hasChanged;
	}

	protected abstract boolean updateContentHash();

}

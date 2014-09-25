package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.security.EncryptionUtil;

public class FileLeaf implements FileComponent{
	private Action action;
	private Path path;
	private String contentHash;
	private FolderComposite parent;
	private boolean isUploaded;
	private boolean maintainContentHashes;
	
	public FileLeaf(Path path){
		this.path = path;
		this.action = new Action(path);
		this.contentHash = "";
		this.isUploaded = false;
		updateContentHash();
	}
	
	public FileLeaf(Path path, boolean maintainContentHashes){
		this.path = path;
		this.action = new Action(path);
		this.contentHash = "";
		this.isUploaded = false;
		if(maintainContentHashes){
			updateContentHash();
		}

	}

	@Override
	public Action getAction() {
		return this.action;
	}

	@Override
	public void putComponent(String path, FileComponent component) {
		// TODO Auto-generated method stub
		System.err.println("put on file not defined.");
	}

	@Override
	public FileComponent deleteComponent(String path) {
		System.err.println("delete on file not defined");
		return null;
	}

	@Override
	public FileComponent getComponent(String path) {
		System.err.println("get on file not defined");
		return null;
	}
	
	@Override
	public String getContentHash() {
		return contentHash;
	}

	@Override
	public void bubbleContentHashUpdate() {
		boolean hasChanged = updateContentHash();
		if(hasChanged){
			parent.bubbleContentHashUpdate();
		}
	}

	@Override
	public void setParent(FolderComposite parent) {
		this.parent = parent;
	}

	@Override
	public Path getPath() {
		return this.path;
	}
	
	/**
	 * Computes and updates this FileLeafs contentHash property.
	 * @return true if the contentHash hash changed, false otherwise
	 */
	@Override
	public boolean updateContentHash() {
		String newHash = "";
		if(path != null && path.toFile() != null){
			try {
				byte[] rawHash = EncryptionUtil.generateMD5Hash(path.toFile());
				if(rawHash != null){
					newHash = Action.createStringFromByteArray(rawHash);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!contentHash.equals(newHash)){
			contentHash = newHash;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public FolderComposite getParent() {
		return this.parent;
	}

	@Override
	public boolean getIsUploaded() {
		return this.isUploaded;
	}

	@Override
	public void setIsUploaded(boolean isUploaded) {
		this.isUploaded = isUploaded;
	}
}

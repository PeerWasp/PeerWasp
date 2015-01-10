package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;

import org.peerbox.watchservice.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLeaf extends AbstractFileComponent{
	
	private static final Logger logger = LoggerFactory.getLogger(FileLeaf.class);
	
	public FileLeaf(final Path path) {
		this(path, true);
	}

	public FileLeaf(final Path path, boolean updateContentHash) {
		super(path, updateContentHash);

		if (updateContentHash) {
			updateContentHash(null);
		}
	}
	
	@Override
	public void bubbleContentHashUpdate(String contentHash) {
		boolean hasChanged = updateContentHash(contentHash);
		if(hasChanged){
			getParent().bubbleContentHashUpdate();
		}
	}
	
	/**
	 * Computes and updates this FileLeafs contentHash property.
	 * @return true if the contentHash hash changed, false otherwise.
	 * @param newHash provided content hash. If this is null, the content hash is
	 * calculated on the fly. If this is not null, it is assumed to be the correct
	 * hash of the file's content at the time of the call.
	 */

	private boolean updateContentHash(String newHash) {
		if(newHash == null){
			newHash = PathUtils.computeFileContentHash(getPath());
		} 
		if(!getContentHash().equals(newHash)){
			setContentHash(newHash);
			
			return true;
		} else {
//			logger.debug("No content hash update: {}", contentHash);
			return false;
		}
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public boolean isReady() {
		if(getParent().getActionIsUploaded()){
			return true;
		}
		return false;
	}

	@Override
	public void propagateIsUploaded() {
		setActionIsUploaded(true);
		getParent().propagateIsUploaded();
	}

}

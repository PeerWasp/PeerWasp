package org.peerbox.delete;

import java.nio.file.Path;

import org.peerbox.watchservice.IFileEventManager;

import com.google.inject.Inject;

public class FileDeleteHandler implements IFileDeleteHandler {

	private IFileEventManager eventManager;
	
	@Inject
	public void setFileEventManager(IFileEventManager eventManager) {
		this.eventManager = eventManager;
	}
	
	@Override
	public void deleteFile(Path fileToDelete) {
		eventManager.onLocalFileHardDelete(fileToDelete);
	}

}

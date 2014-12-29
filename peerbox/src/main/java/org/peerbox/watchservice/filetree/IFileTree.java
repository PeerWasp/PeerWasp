package org.peerbox.watchservice.filetree;

import java.nio.file.Path;

import org.peerbox.watchservice.filetree.composite.FileComponent;

public interface IFileTree {

	public void putFile(FileComponent fileToPut);
	public FileComponent getFile(Path fileToGet);
	public FileComponent updateFile(Path fileToUpdate);
	public FileComponent deleteFile(Path fileToDelete);
}

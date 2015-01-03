package org.peerbox.watchservice.filetree;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

import com.google.common.collect.SetMultimap;

public interface IFileTree {

	public void putFile(Path path, FileComponent fileToPut);
	public FileComponent getFile(Path fileToGet);
	public FileComponent updateFile(Path fileToUpdate);
	public FileComponent deleteFile(Path fileToDelete);
	public Set<Path> getSynchronizedFiles();
	public Path getRootPath();
	
	public SetMultimap<String, FileComponent> getDeletedByContentHash();
	public Map<String, FolderComposite> getDeletedByContentNamesHash();
	public FileComponent findDeletedByContent(FileComponent createdComponent);
	public FileComponent getOrCreateFileComponent(Path path, IFileEventManager eventManager);
}

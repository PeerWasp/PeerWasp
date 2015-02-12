package org.peerbox.watchservice.filetree;

import java.nio.file.Path;
import java.util.Set;

import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

import com.google.common.collect.SetMultimap;

public interface IFileTree {

	void putFile(Path path, FileComponent fileToPut);
	FileComponent getFile(Path fileToGet);
	FileComponent updateFile(Path fileToUpdate);
	FileComponent deleteFile(Path fileToDelete);
	Set<Path> getSynchronizedPathsAsSet();
	Path getRootPath();

	SetMultimap<String, FileComponent> getDeletedByContentHash();
	SetMultimap<String, FileComponent> getCreatedByContentHash();
	SetMultimap<String, FolderComposite> getDeletedByStructureHash();
	SetMultimap<String, FolderComposite> getCreatedByStructureHash();

	FileComponent findDeletedByContent(FileComponent createdComponent);
	FileComponent findCreatedByContent(FileComponent deletedComponent);
	FolderComposite findCreatedByStructure(FolderComposite deletedFolder);
	FolderComposite findDeletedByStructure(FolderComposite createdFolder);
	FileComponent getOrCreateFileComponent(Path path, IFileEventManager eventManager);

	void persistFile(FileComponent file);
	void persistFileAndDescendants(FileComponent root);
}

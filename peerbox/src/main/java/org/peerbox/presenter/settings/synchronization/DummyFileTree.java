package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;

import com.google.common.collect.SetMultimap;

public class DummyFileTree implements IFileTree{

    private Set<Path> synchronizedFiles = new ConcurrentHashSet<Path>();

    public DummyFileTree(DummyUserConfig userConfig){
    	synchronizedFiles = SynchronizationTestUtils.generateLocalFiles(userConfig);
    }
	@Override
	public void putFile(Path path, FileComponent fileToPut) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public FileComponent getFile(Path fileToGet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileComponent updateFile(Path fileToUpdate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileComponent deleteFile(Path fileToDelete) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getRootPath() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SetMultimap<String, FileComponent> getDeletedByContentHash() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SetMultimap<String, FolderComposite> getDeletedByStructureHash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileComponent getOrCreateFileComponent(Path path, IFileEventManager eventManager) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Set<Path> getSynchronizedPathsAsSet() {
		// TODO Auto-generated method stub
		return synchronizedFiles;
	}
	@Override
	public SetMultimap<String, FileComponent> getCreatedByContentHash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetMultimap<String, FolderComposite> getCreatedByStructureHash() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FolderComposite findCreatedByStructure(FolderComposite deletedFolder) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FolderComposite findDeletedByStructure(FolderComposite createdFolder) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FileLeaf findDeletedByContent(FileLeaf createdComponent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FileLeaf findCreatedByContent(FileLeaf deletedComponent) {
		// TODO Auto-generated method stub
		return null;
	}
//	@Override
//	public void persistFile(FileComponent file) {
//		// TODO Auto-generated method stub
//
//	}
//	@Override
//	public void persistFileAndDescendants(FileComponent root) {
//		// TODO Auto-generated method stub
//
//	}

	@Override
	public List<FileComponent> asList() {
		return null;
	}

}

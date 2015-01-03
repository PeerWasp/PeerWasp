package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
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
	public Set<Path> getSynchronizedFiles() {
		// TODO Auto-generated method stub
		return synchronizedFiles;
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
	public Map<String, FolderComposite> getDeletedByContentNamesHash() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FileComponent findDeletedByContent(FileComponent createdComponent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FileComponent getOrCreateFileComponent(Path path, IFileEventManager eventManager) {
		// TODO Auto-generated method stub
		return null;
	}

}

package org.peerbox.watchservice.filetree;

import java.nio.file.Path;

import org.peerbox.selectivesync.ISynchronize;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FolderComposite;

public class FileTree implements IFileTree{
	
	private IContentMaintainer contentMaintainer;
	private ISynchronize synchronizer;
	
	private FolderComposite rootOfFileTree;
	
	public FileTree(Path rootPath, IContentMaintainer contentMaintainer, ISynchronize synchronizer){
		this.contentMaintainer = contentMaintainer;
		this.synchronizer = synchronizer;
		
		rootOfFileTree = new FolderComposite(rootPath, true, true);
	}
	@Override
	public void putFile(FileComponent fileToPut) {
		// TODO Auto-generated method stub
		rootOfFileTree.putComponent(fileToPut.getPath().toString(), fileToPut);
	}

	@Override
	public FileComponent getFile(Path fileToGet) {
		// TODO Auto-generated method stub
		return rootOfFileTree.getComponent(fileToGet.toString());
	}

	@Override
	public FileComponent deleteFile(Path fileToDelete) {
		// TODO Auto-generated method stub
		return rootOfFileTree.deleteComponent(fileToDelete.toString());
	}

	@Override
	public FileComponent updateFile(Path fileToUpdate) {
		// TODO Auto-generated method stub
		return null;
	}

}

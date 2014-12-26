package org.peerbox.presenter.settings.synchonization;

import java.io.File;

import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.IPeerboxFileManager;
import org.peerbox.h2h.ProcessHandle;

public class DummyFileManager implements IPeerboxFileManager {

	@Override
	public ProcessHandle<Void> add(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessHandle<Void> update(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessHandle<Void> delete(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessHandle<Void> move(File source, File destination) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessHandle<Void> download(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessHandle<Void> recover(File file, IVersionSelector versionSelector) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProcessComponent<Void> share(File folder, String userId, PermissionType permission) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileNode listFiles(IProcessComponentListener listener) {
		// TODO Auto-generated method stub
		return generateDummyFileTree();
	}

	private FileNode generateDummyFileTree() {
		// TODO Auto-generated method stub
		return null;
//		File file = new File()
//		return FileNode(n)
	}

	@Override
	public FileNode listFiles() {
		// TODO Auto-generated method stub
		return null;
	}

}

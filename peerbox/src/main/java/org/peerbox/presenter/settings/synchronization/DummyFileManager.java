package org.peerbox.presenter.settings.synchronization;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.IPeerboxFileManager;
import org.peerbox.h2h.ProcessHandle;

import com.google.inject.Inject;

public class DummyFileManager implements IPeerboxFileManager {
	private DummyUserConfig userConfig = new DummyUserConfig();
	
	@Inject
	public DummyFileManager(){
		
	}
	
	@Override
	public ProcessHandle<Void> add(File file) {
		return null;
	}

	@Override
	public ProcessHandle<Void> update(File file) {
		return null;
	}

	@Override
	public ProcessHandle<Void> delete(File file) {
		return null;
	}

	@Override
	public ProcessHandle<Void> move(File source, File destination) {
		return null;
	}

	@Override
	public ProcessHandle<Void> download(File file) {
		return null;
	}

	@Override
	public ProcessHandle<Void> recover(File file, IVersionSelector versionSelector) {
		return null;
	}

	@Override
	public IProcessComponent<Void> share(File folder, String userId, PermissionType permission) {
		return null;
	}

	@Override
	public FileNode listFiles(IProcessComponentListener listener) {
		return listFiles();
	}

	@Override
	public FileNode listFiles() {
		Set<Path> remoteFiles = SynchronizationTestUtils.generateRemoteFiles(userConfig);
		Map<Path, FileNode> tmpMap = new HashMap<Path, FileNode>();
		Path rootPath = userConfig.getRootPath();
		FileNode root = new FileNode(null, rootPath.toFile(), rootPath.toString(), null, null);
		tmpMap.put(rootPath, root);
		for(Path path : remoteFiles){

			FileNode parent = tmpMap.get(path.getParent());
			File file = path.toFile();
			file.isDirectory();
			String pathStr = path.toString();
			FileNode current = new FileNode(parent, file, pathStr, null, null);
			System.out.println("File: " + path.toString() + ": " + file.isDirectory() );
			if(parent != null){
				parent.getChildren().add(current);
			}

			tmpMap.put(path, current);
		}
		return root;
	}

}

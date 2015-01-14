package org.peerbox.presenter.settings.synchronization;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.ProcessComposite;
import org.hive2hive.processframework.ProcessState;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.h2h.ProcessHandle;

import com.google.inject.Inject;

public class DummyFileManager implements IFileManager {
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
	public ProcessHandle<Void> share(File folder, String userId, PermissionType permission) {
		return null;
	}

	@Override
	public ProcessHandle<FileNode> listFiles() {
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

		ProcessHandle<FileNode> handle = new ProcessHandle<FileNode>(new DummyProcessComponent(root));
		return handle;
	}

	@Override
	public boolean existsRemote(Path path) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isSmallFile(Path path) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isLargeFile(Path path) {
		// TODO Auto-generated method stub
		return false;
	}

	private class DummyProcessComponent implements IProcessComponent<FileNode> {

		private FileNode node;

		public DummyProcessComponent(FileNode node) {
			this.node = node;
		}

		@Override
		public FileNode execute() throws InvalidProcessStateException, ProcessExecutionException {
			return node;
		}

		@Override
		public Future<FileNode> executeAsync() throws InvalidProcessStateException,
				ProcessExecutionException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileNode rollback() throws InvalidProcessStateException, ProcessRollbackException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<FileNode> rollbackAsync() throws InvalidProcessStateException,
				ProcessRollbackException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void pause() throws InvalidProcessStateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void resume() throws InvalidProcessStateException, ProcessExecutionException,
				ProcessRollbackException {
			// TODO Auto-generated method stub

		}

		@Override
		public void attachListener(IProcessComponentListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public void detachListener(IProcessComponentListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setName(String name) {
			// TODO Auto-generated method stub

		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setParent(ProcessComposite<?> parent) {
			// TODO Auto-generated method stub

		}

		@Override
		public ProcessComposite<?> getParent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getID() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ProcessState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getProgress() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<IProcessComponentListener> getListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean getRollbackRequired() {
			// TODO Auto-generated method stub
			return false;
		}

	}

}

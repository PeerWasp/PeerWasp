package org.peerbox.app.manager.file;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.config.IUserConfig;
import org.peerbox.app.manager.AbstractManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.events.MessageBus;
import org.peerbox.h2h.ProcessHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


public class FileManager extends AbstractManager implements IFileManager {

	private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

	@Inject
	public FileManager(final INodeManager nodeManager, final IUserConfig userConfig, final MessageBus messageBus) {
		super(nodeManager, userConfig, messageBus);
	}

	@Override
	public ProcessHandle<Void> add(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("ADD - {}", file);
		IProcessComponent<Void> component = getH2HFileManager().createAddProcess(file);
		component.attachListener(new FileUploadListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> update(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("UPDATE - {}", file);
		IProcessComponent<Void> component = getH2HFileManager().createUpdateProcess(file);
		component.attachListener(new FileUploadListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> delete(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("DELETE - {}", file);
		IProcessComponent<Void> component = getH2HFileManager().createDeleteProcess(file);
		component.attachListener(new FileDeleteListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> move(final File source, final File destination) throws NoSessionException, NoPeerConnectionException {
		logger.debug("MOVE - from: {}, to: {}", source, destination);
		IProcessComponent<Void> component = getH2HFileManager().createMoveProcess(source, destination);
		component.attachListener(new FileDeleteListener(source));
		component.attachListener(new FileUploadListener(destination));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> download(final File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("DOWNLOAD - {}", file);
		IProcessComponent<Void> component = getH2HFileManager().createDownloadProcess(file);
		component.attachListener(new FileDownloadListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> recover(final File file, final IVersionSelector versionSelector) throws NoSessionException, NoPeerConnectionException, IllegalArgumentException {
		logger.debug("RECOVER - {}", file);
		IProcessComponent<Void> component = getH2HFileManager().createRecoverProcess(file, versionSelector);
		component.attachListener(new FileDownloadListener(file));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<Void> share(final File folder, final String userId, final PermissionType permission) throws IllegalArgumentException, NoSessionException, NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException {
		logger.debug("SHARE - User: '{}', Permission: '{}', Folder: '{}'", userId, permission.name(), folder);
		IProcessComponent<Void> component = getH2HFileManager().createShareProcess(folder, userId, permission);
		component.attachListener(new FileOperationListener(folder));
		ProcessHandle<Void> handle = new ProcessHandle<Void>(component);
		return handle;
	}

	@Override
	public ProcessHandle<FileNode> listFiles() throws NoPeerConnectionException, NoSessionException {
		IProcessComponent<FileNode> component = getH2HFileManager().createFileListProcess();
		component.attachListener(new FileOperationListener(new File("listFiles")));
		ProcessHandle<FileNode> handle = new ProcessHandle<FileNode>(component);
		return handle;
	}

	@Override
	public boolean existsRemote(final Path path) {
		FileNode item = null;
		FileNode list = null;
		try {
			list = listFiles().execute();
			item = getFileNodeByPath(list, path);
		} catch (NoPeerConnectionException | NoSessionException |
				InvalidProcessStateException | ProcessExecutionException e) {
			item = null;
			logger.warn("Could not check existsRemote - Exception: {}", e.getMessage(), e);
		}

		return item != null ? true : false;
	}

	/**
	 * Searches a FileNode given a path.
	 * @param index the file index, e.g. the root node
	 * @param path the path for which the node should be searched
	 * @return the file node corresponding to the given path or null if none exists
	 */
	private FileNode getFileNodeByPath(final FileNode index, final Path path) {
		Path current = path;
		List<String> pathItems = new ArrayList<>();
		while (current != null && !getRootPath().equals(current)) {
			pathItems.add(current.getFileName().toString());
			current = current.getParent();
		}
		Collections.reverse(pathItems);

		FileNode currentNode = index;
		for (String pathItem : pathItems) {
			FileNode child = getChildByName(currentNode.getChildren(), pathItem);
			if (child == null) {
				return null;
			}

			currentNode = child;
			if(child.isFile()) {
				break; // cannot go further down the tree
			}
		}
		// it may be the case that we did not consider all pathItems
		if(currentNode.getFile().toPath().equals(path)) {
			return currentNode;
		} else {
			return null;
		}
	}

	/**
	 * Searches a child node in a list with a given name
	 * @param children list of child nodes
	 * @param name the name to search
	 * @return the FileNode or null if none exists
	 */
	private FileNode getChildByName(final List<FileNode> children, final String name) {
		for (FileNode child : children) {
			if (child.getName().equalsIgnoreCase(name)) {
				return child;
			}
		}
		return null;
	}

	@Override
	public boolean isSmallFile(final Path path) {
		IFileConfiguration fileConfig = getFileConfiguration();
		BigInteger max = fileConfig.getMaxFileSize();
		BigInteger size = BigInteger.valueOf(FileUtil.getFileSize(path.toFile()));
		return size.compareTo(max) == -1;
	}

	@Override
	public boolean isLargeFile(final Path path) {
		return !isSmallFile(path);
	}

	private void notifyFileUpload(final Path path) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new FileUploadMessage(path));
		}
	}

	private void notifyFileDownload(final Path path) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new FileDownloadMessage(path));
		}
	}

	private void notifyFileDelete(final Path path) {
		if (getMessageBus() != null) {
			getMessageBus().publish(new FileDeleteMessage(path));
		}
	}

	private class FileOperationListener implements IProcessComponentListener {

		private final File path;

		FileOperationListener(final File path) {
			this.path = path;
		}

		public File getPath() {
			return path;
		}

		@Override
		public void onExecuting(IProcessEventArgs args) {
			logger.trace("onExecuting: {}", path);
		}

		@Override
		public void onRollbacking(IProcessEventArgs args) {
			logger.trace("onRollbacking: {}", path);
		}

		@Override
		public void onPaused(IProcessEventArgs args) {
			logger.trace("onPaused: {}", path);
		}

		@Override
		public void onExecutionSucceeded(IProcessEventArgs args) {
			logger.trace("onExecutionSucceeded: {}", path);
		}

		@Override
		public void onExecutionFailed(IProcessEventArgs args) {
			logger.trace("onExecutionFailed: {}", path);
		}

		@Override
		public void onRollbackSucceeded(IProcessEventArgs args) {
			logger.trace("onRollbackSucceeded: {}", path);
		}

		@Override
		public void onRollbackFailed(IProcessEventArgs args) {
			logger.trace("onRollbackFailed: {}", path);
		}

	}

	private class FileUploadListener extends FileOperationListener {

		FileUploadListener(final File path) {
			super(path);
		}

		@Override
		public void onExecutionSucceeded(IProcessEventArgs args) {
			super.onExecutionSucceeded(args);
			notifyFileUpload(getPath().toPath());
		}

	}

	private class FileDownloadListener extends FileOperationListener {

		FileDownloadListener(final File path) {
			super(path);
		}

		@Override
		public void onExecutionSucceeded(IProcessEventArgs args) {
			super.onExecutionSucceeded(args);
			notifyFileDownload(getPath().toPath());
		}

	}

	private class FileDeleteListener extends FileOperationListener {

		FileDeleteListener(final File path) {
			super(path);
		}

		@Override
		public void onExecutionSucceeded(IProcessEventArgs args) {
			super.onExecutionSucceeded(args);
			notifyFileDelete(getPath().toPath());
		}

	}
}
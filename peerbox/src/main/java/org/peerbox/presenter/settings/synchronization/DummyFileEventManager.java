package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.peerbox.events.MessageBus;
import org.peerbox.watchservice.ActionQueue;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.IFileTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class DummyFileEventManager implements IFileEventManager{

	private static final Logger logger = LoggerFactory.getLogger(DummyFileEventManager.class);
    private IFileTree fileTree;

    @Inject
	public DummyFileEventManager(){
		DummyUserConfig userConfig = new DummyUserConfig();
		fileTree = new DummyFileTree(userConfig);
	}
	@Override
	public IFileTree getFileTree() {
		return fileTree;
	}

	@Override
	public ActionQueue getFileComponentQueue() {
		return null;
	}

	@Override
	public void onFileDesynchronized(Path path) {
		logger.debug("Triggered desynchronization of {}", path);
	}
	@Override
	public void onFileSynchronized(Path path, boolean isFolder) {
		logger.debug("Triggered synchronization of {}", path);
	}
	@Override
	public void onFileAdd(IFileAddEvent fileEvent) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onLocalFileHardDelete(Path toDelete) {
		// TODO Auto-generated method stub

	}
	@Override
	public MessageBus getMessageBus() {
		// TODO Auto-generated method stub
		return null;
	}

}

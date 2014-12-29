package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.peerbox.selectivesync.ISynchronize;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;

public class DummyFileEventManager implements IFileEventManager{

	private static final Logger logger = LoggerFactory.getLogger(DummyFileEventManager.class);
    private Set<Path> synchronizedFiles = new ConcurrentHashSet<Path>();
    
    @Inject
	public DummyFileEventManager(){
		DummyUserConfig userConfig = new DummyUserConfig();
		synchronizedFiles = SynchronizationTestUtils.generateLocalFiles(userConfig);
	}
	@Override
	public FileTree getFileTree() {
		return null;
	}

	@Override
	public BlockingQueue<FileComponent> getFileComponentQueue() {
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

}

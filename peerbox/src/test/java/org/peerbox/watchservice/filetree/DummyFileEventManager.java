package org.peerbox.watchservice.filetree;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FolderComposite;
import org.peerbox.watchservice.IFileEventManager;

import com.google.common.collect.SetMultimap;

public class DummyFileEventManager implements IFileEventManager{

	@Override
	public FolderComposite getFileTree() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockingQueue<FileComponent> getFileComponentQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetMultimap<String, FileComponent> getDeletedFileComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileComponent findDeletedByContent(FileComponent file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileComponent deleteFileComponent(Path path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, FolderComposite> getDeletedByContentNamesHash() {
		// TODO Auto-generated method stub
		return null;
	}

}

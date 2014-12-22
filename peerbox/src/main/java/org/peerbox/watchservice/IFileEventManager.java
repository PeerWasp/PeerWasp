package org.peerbox.watchservice;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.peerbox.selectivesync.ISynchronize;

import com.google.common.collect.SetMultimap;

public interface IFileEventManager {

	public FolderComposite getFileTree();
	public BlockingQueue<FileComponent> getFileComponentQueue();
	public SetMultimap<String, FileComponent> getDeletedFileComponents();
	public FileComponent findDeletedByContent(FileComponent file);

	public Map<String, FolderComposite> getDeletedByContentNamesHash();
	
	public ISynchronize getSynchronizer();
}

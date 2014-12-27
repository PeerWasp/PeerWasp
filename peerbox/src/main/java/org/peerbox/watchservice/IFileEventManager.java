package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.hive2hive.core.events.implementations.FileUpdateEvent;
import org.peerbox.selectivesync.ISynchronize;

import com.google.common.collect.SetMultimap;

public interface IFileEventManager {

	public FolderComposite getFileTree();
	public BlockingQueue<FileComponent> getFileComponentQueue();
	public SetMultimap<String, FileComponent> getDeletedFileComponents();
	public FileComponent findDeletedByContent(FileComponent file);

	public Map<String, FolderComposite> getDeletedByContentNamesHash();
	
	public ISynchronize getSynchronizer();
	
    public Set<Path> getSynchronizedFiles();
    
	public void onFileDesynchronized(Path path);
	public void onFileSynchronized(Path path, boolean isFolder);

}

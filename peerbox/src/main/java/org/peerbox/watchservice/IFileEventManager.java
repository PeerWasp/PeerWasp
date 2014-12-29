package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.peerbox.selectivesync.ISynchronize;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;

import com.google.common.collect.SetMultimap;

public interface IFileEventManager {

	public FileTree getFileTree();
	public BlockingQueue<FileComponent> getFileComponentQueue();
//	public SetMultimap<String, FileComponent> getDeletedFileComponents();
//	public FileComponent findDeletedByContent(FileComponent file);
    
	public void onFileDesynchronized(Path path);
	public void onFileSynchronized(Path path, boolean isFolder);

}

package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.google.common.collect.SetMultimap;

public interface IFileEventManager {

	public FolderComposite getFileTree();
	public BlockingQueue<FileComponent> getFileComponentQueue();
	public SetMultimap<String, FileComponent> getDeletedFileComponents();
	public FileComponent findDeletedByContent(FileComponent file);
	public FileComponent deleteFileComponent(Path path);
	public Map<String, FolderComposite> getDeletedByContentNamesHash();
}

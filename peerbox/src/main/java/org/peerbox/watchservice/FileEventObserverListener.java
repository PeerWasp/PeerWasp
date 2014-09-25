package org.peerbox.watchservice;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.api.interfaces.IFileObserverListener;

public class FileEventObserverListener implements IFileObserverListener {

	private FileEventManager manager;
	
	public FileEventObserverListener(FileEventManager manager){
		this.manager = manager;
	}
	
	@Override
	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onDirectoryCreate(File directory) {
		// TODO Auto-generated method stub
		manager.onFileCreated(directory.toPath(), true);
	}

	@Override
	public void onDirectoryChange(File directory) {
		// TODO Auto-generated method stub
		manager.onFileModified(directory.toPath());
	}

	@Override
	public void onDirectoryDelete(File directory) {
		// TODO Auto-generated method stub
		manager.onFileDeleted(directory.toPath());
	}

	@Override
	public void onFileCreate(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileChange(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileDelete(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub

	}
}

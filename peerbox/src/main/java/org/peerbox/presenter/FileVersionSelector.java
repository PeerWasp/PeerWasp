package org.peerbox.presenter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.peerbox.interfaces.IFileVersionSelectorEventListener;

final public class FileVersionSelector implements IVersionSelector {
	
	private final Lock selectionLock = new ReentrantLock();
	private final Condition versionSelectedCondition  = selectionLock.newCondition();
	private final IFileVersionSelectorEventListener listener; 
	private IFileVersion selectedVersion;
	private String recoveredFileName;
	
	private final AtomicBoolean gotAvailableVersions = new AtomicBoolean();
	private final AtomicBoolean isCancelled = new AtomicBoolean();
	private final AtomicBoolean hasSelected = new AtomicBoolean();
	
	public FileVersionSelector(final IFileVersionSelectorEventListener listener) {
		if(listener == null) {
			throw new IllegalArgumentException("Argument listener must not be null.");
		}
		
		this.gotAvailableVersions.set(false);
		this.isCancelled.set(false);
		this.hasSelected.set(false);
		this.recoveredFileName = "";
		this.listener = listener;
	}
	
	public void cancel() {
		isCancelled.set(true);
		selectedVersion = null;
	}
	
	public void selectVersion(IFileVersion selectedVersion)  {
		if(hasSelected.get()) {
			throw new IllegalStateException("Cannot select multiple times.");
		}
		hasSelected.set(true);
		
		if(!gotAvailableVersions.get()) {
			throw new IllegalStateException("Cannot select version before retrieving available versions");
		}
		
		if(selectedVersion == null) {
			cancel();
		}
		
		try {
			selectionLock.lock();
			this.selectedVersion = selectedVersion;
			versionSelectedCondition.signal(); // wake up other waiting thread 
		} finally {
			selectionLock.unlock();
		}
	}

	@Override
	public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
		if(isCancelled.get()) {
			// not interested in versions anymore -> select nothing (i.e. cancel)
			return null;
		}
		
		try {
			selectionLock.lock();
			if(availableVersions != null) {
				gotAvailableVersions.set(true);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				listener.onAvailableVersionsReceived(availableVersions);
				// wait here until selectVersion(version) is called
				versionSelectedCondition.awaitUninterruptibly();
			}
		} finally {
			selectionLock.unlock();
		}
		
		return selectedVersion;
	}

	public String getRecoveredFileName() {
		return recoveredFileName;
	}

	@Override
	public String getRecoveredFileName(String fullName, String name, String extension) {
		if(isCancelled.get()) {
			return null;
		}
		
		// generate a new file name indicating that the file is restored
		Date versionDate = new Date(selectedVersion.getDate());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
		
		String newFileName = String.format("%s-%s", name, sdf.format(versionDate));
		if(extension!=null && extension.length() > 0) {
			newFileName = String.format("%s.%s", newFileName, extension);
		}
		recoveredFileName = newFileName;
		return newFileName;
	}
}

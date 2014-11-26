package org.peerbox.presenter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.peerbox.interfaces.IFileVersionSelectorEventListener;

class FileVersionSelector implements IVersionSelector {
	
	private final Lock selectionLock = new ReentrantLock();
	private final Condition versionSelectedCondition  = selectionLock.newCondition();
	private IFileVersionSelectorEventListener listener; 
	private IFileVersion selectedVersion;
	private String recoveredFileName;
	
	private volatile boolean gotAvailableVersions = false;
	private volatile boolean isCancelled = false;
	
	public FileVersionSelector(IFileVersionSelectorEventListener listener) {
		if(listener == null) {
			throw new IllegalArgumentException("Argument listener must not be null.");
		}
		
		this.recoveredFileName = "";
		this.listener = listener;
	}
	
	public void selectVersion(IFileVersion selectedVersion) {
		if(selectedVersion == null) {
			isCancelled = true;
		}
		
		if(gotAvailableVersions) {
			try {
				selectionLock.lock();
				this.selectedVersion = selectedVersion;
				versionSelectedCondition.signal();
			} finally {
				selectionLock.unlock();
			}
		}
	}

	@Override
	public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
		
		if(isCancelled) {
			// not interested in versions anymore -> select nothing (i.e. cancel)
			return null;
		}
		
		try {
			selectionLock.lock();
			if(availableVersions != null) {
				gotAvailableVersions = true;
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

package org.peerbox.filerecovery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.files.recover.IVersionSelector;

public final class FileVersionSelector implements IVersionSelector {

	private final CountDownLatch doneSignal;
	private final IFileVersionSelectorListener listener;
	private IFileVersion selectedVersion;
	private String recoveredFileName;

	private final AtomicBoolean gotAvailableVersions = new AtomicBoolean();
	private final AtomicBoolean isCancelled = new AtomicBoolean();
	private final AtomicBoolean hasSelected = new AtomicBoolean();

	public FileVersionSelector(final IFileVersionSelectorListener listener) {
		if(listener == null) {
			throw new IllegalArgumentException("Argument listener must not be null.");
		}

		this.doneSignal = new CountDownLatch(1);
		this.gotAvailableVersions.set(false);
		this.isCancelled.set(false);
		this.hasSelected.set(false);
		this.recoveredFileName = "";
		this.listener = listener;
	}

	public void cancel() {
		isCancelled.set(true);
		selectedVersion = null;
		doneSignal.countDown();
	}

	public boolean isCancelled() {
		return isCancelled.get();
	}

	public void selectVersion(IFileVersion selectedVersion)  {
		try {
			if(hasSelected.get()) {
				throw new IllegalStateException("Calling selectVersion multiple times is not allowed.");
			}
			hasSelected.set(true);

			if(!gotAvailableVersions.get()) {
				throw new IllegalStateException("Cannot select version before retrieving available versions.");
			}

			if(selectedVersion == null) {
				cancel();
			}

			this.selectedVersion = selectedVersion;
		} finally  {
			doneSignal.countDown(); // wake up other waiting thread
		}

	}

	@Override
	public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
		if(isCancelled.get()) {
			// not interested in versions anymore -> select nothing (i.e. cancel)
			return null;
		}

		try {
			if(availableVersions != null) {
				gotAvailableVersions.set(true);

				listener.onAvailableVersionsReceived(availableVersions);

				// wait here until selectVersion(version) is called
				doneSignal.await();
			}
		} catch (InterruptedException e) {
			// happens if e.g. file recovery is cancelled after retrieving available versions
			e.printStackTrace();
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

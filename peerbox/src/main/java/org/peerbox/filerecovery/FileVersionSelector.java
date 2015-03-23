package org.peerbox.filerecovery;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.files.recover.IVersionSelector;

/**
 * File version selector for H2H.
 * It works as follows:
 * - Available versions are received from H2H ({@link #selectVersion(List)}).
 * - This triggers a notification of the listener
 * (forward available versions, {@link IFileVersionSelectorListener#onAvailableVersionsReceived(List)}.
 * - The thread is blocked in {@link #selectVersion(List)} until a version is selected.
 * - The version selector waits using the count down latch until the listener chooses a version by
 * calling {@link #selectVersion(IFileVersion, Path)}.
 * - The selected version is returned (unblock, {@link #selectVersion(List)} continues).
 *
 * @author albrecht
 *
 */
final class FileVersionSelector implements IVersionSelector {

	private final CountDownLatch doneSignal;
	private final IFileVersionSelectorListener listener;
	private IFileVersion selectedVersion;
	private String recoveredFileName;

	private final AtomicBoolean gotAvailableVersions = new AtomicBoolean();
	private final AtomicBoolean isCancelled = new AtomicBoolean();
	private final AtomicBoolean hasSelected = new AtomicBoolean();
	private Path fileToRecover;

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

	/**
	 * Cancel version selection.
	 * Unblocks waiting thread in {@link #selectVersion(List)} (will return null).
	 */
	public void cancel() {
		isCancelled.set(true);
		selectedVersion = null;
		doneSignal.countDown();
	}

	public boolean isCancelled() {
		return isCancelled.get();
	}

	/**
	 * Select a specific version of a file.
	 * Unblocks the waiting thread in {@link #selectVersion(List)}.
	 *
	 * @param selectedVersion the version to recover
	 * @param fileToRecover the path to the file to recover (original, not the new name!)
	 */
	public void selectVersion(IFileVersion selectedVersion, Path fileToRecover)  {
		this.fileToRecover = fileToRecover;

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

	/**
	 * This is called by H2H - receive available versions.
	 * Notifies listener and blocks until version is selected and returns version.
	 */
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

		/*
		 * Search new name for file and make sure that we do not overwrite an existing file:
		 * 1. try pattern oldfilename-date.extension
		 * 2. if file already exists, extend pattern to: oldfilename-date-i.extension where i is
		 * 		a counter until a file is found that does not exist.
		 */
		String newFileName = null;
		String postfix = ""; // set this postfix if file already exists
		int iteration = 0;
		do {
			if(iteration > 0) {
				// file seems to exists - add "-x" to the filename"
				postfix = String.format("-%d", iteration);
			}

			newFileName = String.format("%s-%s%s", name, sdf.format(versionDate), postfix);
			if(extension!=null && extension.length() > 0) {
				newFileName = String.format("%s.%s", newFileName, extension);
			}

			++iteration;
		} while(Files.exists(fileToRecover.getParent().resolve(newFileName)));



		recoveredFileName = newFileName;
		return newFileName;
	}
}

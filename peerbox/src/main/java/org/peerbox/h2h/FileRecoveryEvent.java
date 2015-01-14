package org.peerbox.h2h;

import java.io.File;

import org.hive2hive.core.events.framework.abstracts.FileEvent;

public class FileRecoveryEvent extends FileEvent implements IFileRecoveryRequestEvent {
	
	private int fVersionToRecover = 0;
	public FileRecoveryEvent(File file, int versionToRecover) {
		super(file, true);
		fVersionToRecover = versionToRecover;
	}

	@Override
	public int getVersionToRecover() {
		return fVersionToRecover;
	}

}

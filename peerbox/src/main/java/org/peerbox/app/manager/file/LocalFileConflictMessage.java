package org.peerbox.app.manager.file;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public final class LocalFileConflictMessage extends AbstractFileMessage {

	public LocalFileConflictMessage(FileHelper file) {
		super(file);
	}

}

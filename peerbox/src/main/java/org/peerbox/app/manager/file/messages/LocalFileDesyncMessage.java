package org.peerbox.app.manager.file.messages;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileDesyncMessage extends AbstractFileMessage{

	public LocalFileDesyncMessage(final FileHelper file) {
		super(file);
	}
}

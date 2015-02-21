package org.peerbox.app.manager.file;

import java.nio.file.Path;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalFileDesyncMessage extends AbstractFileMessage{

	public LocalFileDesyncMessage(final FileHelper file) {
		super(file);
	}
}

package org.peerbox.app.manager.file;

import java.nio.file.Path;

import org.peerbox.presenter.settings.synchronization.FileHelper;

public class AbstractFileMessage implements IFileMessage {

	private final FileHelper file;

	public AbstractFileMessage(final FileHelper file) {
		this.file = file;
	}

	@Override
	public FileHelper getFile() {
		return file;
	}

}

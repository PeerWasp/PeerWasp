package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class FileDownloadMessage extends AbstractFileMessage {

	public FileDownloadMessage(Path path) {
		super(path);
	}

}

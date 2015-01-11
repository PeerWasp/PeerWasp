package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class FileUploadMessage extends AbstractFileMessage {

	FileUploadMessage(Path path) {
		super(path);
	}

}

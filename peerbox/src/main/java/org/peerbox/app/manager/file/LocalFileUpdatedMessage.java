package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class LocalFileUpdatedMessage extends AbstractFileMessage {

	public LocalFileUpdatedMessage(Path path) {
		super(path);
	}

}

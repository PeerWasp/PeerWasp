package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class RemoteFileDeletedMessage extends AbstractFileMessage {

	public RemoteFileDeletedMessage(Path path) {
		super(path);
	}

}

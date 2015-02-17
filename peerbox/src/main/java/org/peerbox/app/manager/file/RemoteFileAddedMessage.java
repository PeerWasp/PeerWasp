package org.peerbox.app.manager.file;

import java.nio.file.Path;

public final class RemoteFileAddedMessage extends AbstractFileMessage {

	public RemoteFileAddedMessage(Path path) {
		super(path);
	}

}

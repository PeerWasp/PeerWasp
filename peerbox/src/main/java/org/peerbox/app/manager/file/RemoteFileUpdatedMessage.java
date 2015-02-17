package org.peerbox.app.manager.file;

import java.nio.file.Path;

public class RemoteFileUpdatedMessage extends AbstractFileMessage {

	public RemoteFileUpdatedMessage(Path path) {
		super(path);
	}

}

package org.peerbox.app.manager.file;

import org.peerbox.events.IMessage;

public interface IFileMessage extends IMessage {

	FileInfo getFile();

}

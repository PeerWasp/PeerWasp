package org.peerbox.app.manager.file;

import java.nio.file.Path;

import org.peerbox.events.IMessage;

public interface IFileMessage extends IMessage {

	Path getPath();

}

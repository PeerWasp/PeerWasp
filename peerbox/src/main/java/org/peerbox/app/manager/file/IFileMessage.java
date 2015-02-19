package org.peerbox.app.manager.file;

import java.nio.file.Path;

import org.peerbox.events.IMessage;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public interface IFileMessage extends IMessage {

	FileHelper getFile();

}

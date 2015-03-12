package org.peerbox.app.manager.file;

import org.peerbox.events.IMessage;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public interface IFileMessage extends IMessage {

	FileHelper getFile();

}

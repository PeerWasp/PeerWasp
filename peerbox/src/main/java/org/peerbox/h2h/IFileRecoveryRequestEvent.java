package org.peerbox.h2h;

import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;

public interface IFileRecoveryRequestEvent extends IFileEvent {
	int getVersionToRecover();
}

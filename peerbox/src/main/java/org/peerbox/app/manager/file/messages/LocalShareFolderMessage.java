package org.peerbox.app.manager.file.messages;

import org.hive2hive.core.model.UserPermission;
import org.peerbox.app.manager.file.FileInfo;

public final class LocalShareFolderMessage extends AbstractFileMessage {

	private UserPermission newPermission;

	public LocalShareFolderMessage(FileInfo file, UserPermission newPermission) {
		super(file);
		this.newPermission = newPermission;
	}

	public UserPermission getInvitedUserPermission() {
		return newPermission;
	}

}

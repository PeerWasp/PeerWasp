package org.peerbox.app.manager.file.messages;

import org.hive2hive.core.model.UserPermission;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class LocalShareFolderMessage extends AbstractFileMessage {		
	UserPermission newPermission;
	public LocalShareFolderMessage(FileHelper file, UserPermission newPermission) {
		super(file);
		this.newPermission = newPermission;

	}
	
	public UserPermission getInvitedUserPermission(){
		return newPermission;
	}

}

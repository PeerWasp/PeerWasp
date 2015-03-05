package org.peerbox.app.manager.file;

import java.util.Set;

import org.hive2hive.core.model.UserPermission;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class RemoteShareFolderMessage extends AbstractFileMessage {
	
	Set<UserPermission> permissions;
	String invitedBy;
	public RemoteShareFolderMessage(FileHelper file, Set<UserPermission> permissions, String invitedBy) {
		super(file);
		this.permissions = permissions;
		this.invitedBy = invitedBy;
	}
	
	public Set<UserPermission> getUserPermissions(){
		return permissions;
	}
	
	public String getInvitedBy(){
		return invitedBy;
	}
}
package org.peerbox.app.manager.file.messages;

import java.util.Set;

import org.hive2hive.core.model.UserPermission;
import org.peerbox.app.manager.file.FileInfo;

public final class RemoteShareFolderMessage extends AbstractFileMessage {

	private Set<UserPermission> permissions;
	private String invitedBy;

	public RemoteShareFolderMessage(FileInfo file, Set<UserPermission> permissions, String invitedBy) {
		super(file);
		this.permissions = permissions;
		this.invitedBy = invitedBy;
	}

	public Set<UserPermission> getUserPermissions() {
		return permissions;
	}

	public String getInvitedBy() {
		return invitedBy;
	}

}
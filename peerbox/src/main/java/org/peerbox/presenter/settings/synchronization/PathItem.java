package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import java.util.Set;

import org.hive2hive.core.model.UserPermission;

public class PathItem{
	private Path path;
	private boolean isFile;
	private Set<UserPermission> userPermissions;

	public PathItem(Path path) {
		this(path, true, null);
	}

	public PathItem(Path path, boolean isFile, Set<UserPermission> userPermissions) {
		this.path = path;
		setIsFile(isFile);
		this.userPermissions = userPermissions;
	}

	public Path getPath() {
		return path;
	}
	
	public Set<UserPermission> getUserPermissions(){
		return userPermissions;
	}

	public boolean isFile() {
		return isFile;
	}

	private void setIsFile(boolean isFile) {
		this.isFile = isFile;
	}

	public boolean isFolder() {
		return !isFile;
	}

	@Override
	public String toString() {
		return "";
	}
}
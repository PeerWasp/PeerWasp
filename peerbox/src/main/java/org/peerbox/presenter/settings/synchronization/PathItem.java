package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;

import org.hive2hive.core.model.UserPermission;

public class PathItem{
	private Path path;
	private boolean isFile;
	private SetProperty<UserPermission> permissions;
	private Label label = new Label();

	public PathItem(Path path) {
		this(path, true, new HashSet<UserPermission>());
	}

	public PathItem(Path path, boolean isFile, Set<UserPermission> userPermissions) {
		this.path = path;
		setIsFile(isFile);
		this.permissions = new SimpleSetProperty<UserPermission>(FXCollections.observableSet(userPermissions));
	}

	public void bindPermissionsTo(SetProperty<UserPermission> other){
		Set<UserPermission> oldPermissions = new HashSet<UserPermission>(getPermissions());
		getPermissionsSetProperty().bindContent(other);
		getPermissionsSetProperty().addAll(oldPermissions);
	}

	public Path getPath() {
		return path;
	}

	public Set<UserPermission> getPermissions(){
		return permissions;
	}

	public SetProperty<UserPermission> getPermissionsSetProperty(){
		return permissions;
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

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label){
		this.label = label;
	}

}
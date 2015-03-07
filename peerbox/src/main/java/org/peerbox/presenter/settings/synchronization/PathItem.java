package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.collections.FXCollections;

import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;


public class PathItem{
	private Path path;
	private boolean isFile;
//	private ObservableSet<UserPermission> permissionsProperty;
//	private Set<UserPermission> permissions;
//	private PermissionProperty ownPermissions;
//	private PermissionBinding permissionSummary;
//	private ObjectProperty<ObservableSet<UserPermission>> ownPermissions;
	
	private SetProperty<UserPermission> permissions;

	public PathItem(Path path) {
		this(path, true, null);
	}
	
	public PathItem(Path path, boolean isFile, Set<UserPermission> userPermissions) {
		this.path = path;
		setIsFile(isFile);
		this.permissions = new SimpleSetProperty<UserPermission>(FXCollections.observableSet(userPermissions));
	}
	
	public void bind(SetProperty<UserPermission> other){
		Set<UserPermission> oldPermissions = getUserPermissions();
		getPermissionsSetProperty().bindContent(other);
		getPermissionsSetProperty().addAll(oldPermissions);
	}
	
//	public void addPermissionsListener(PathItem listener){
//	permissionsProperty.addListener(listener);
//}
//
//public void removePermissionsListener(PathItem listener){
//	permissionsProperty.removeListener(listener);
//}

	public Path getPath() {
		return path;
	}
	
	public Set<UserPermission> getUserPermissions(){
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

//	@Override
//	public void onChanged(
//			javafx.collections.SetChangeListener.Change<? extends UserPermission> arg0) {
//		if(arg0.wasAdded()){
//			permissionsProperty.add(arg0.getElementAdded());
//		} else {
//			permissionsProperty.remove(arg0.getElementAdded());
//		}
//	}
}
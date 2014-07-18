package org.peerbox.presenter;

import java.io.File;
import java.io.IOException;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.peerbox.PropertyHandler;
import org.peerbox.model.H2HManager;

public class SelectRootPathUtils {

	public static void showPermissionWarning() {
		Dialogs.create().title("Directory creation failed")
			.message("Either the selected directory's parent directory does not exist or you don't have permissions to create the directory.")
			.showWarning();
	}

	public static Action askForDirectoryCreation() {
		return Dialogs.create()
			      .title("Directory does not exist.")
			      .message( "Create this directory?")
			      .showConfirm();
	}

	public static void showIncorrectSelectionInformation() {
		Dialogs.create().title("File instead of directory selected.")
		.message("Please change the path as it does not lead to a directory")
		.showInformation();
	}

	public static void verifyRootPath(String desiredRootPath) {
		try {
			File path = new File(desiredRootPath);
			Action createDirAction = Dialog.Actions.YES;
			boolean isDirCreated = false;
			if(!path.exists()){
				createDirAction = SelectRootPathUtils.askForDirectoryCreation();
			}
			
			//TODO rootpath should be read from H2HManager!
			if(createDirAction.equals(Dialog.Actions.YES)){
				isDirCreated = H2HManager.INSTANCE.initializeRootDirectory(desiredRootPath);
				if(isDirCreated){
					PropertyHandler.setRootPath(desiredRootPath); //save path in property file
					MainNavigator.navigate("/org/peerbox/view/LoginView.fxml");
				} else {
					SelectRootPathUtils.showPermissionWarning();
				}
			}
		} catch (IOException e) {
			SelectRootPathUtils.showIncorrectSelectionInformation();
		}
	}

	public static void showInvalidDirectoryChooserEntryInformation() {
		Dialogs.create().title("Can't open the directory.")
		.message("The selected directory and its parent directory do not exist.")
		.showInformation();
	}

	public static String showDirectoryChooser(String pathAsString,
			Window toOpenDialog) {

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose your root directory");
		chooser.setInitialDirectory(new File(pathAsString).getParentFile());
		try {
			File selectedDirectory = chooser.showDialog(toOpenDialog);
			if (selectedDirectory != null) {
				return selectedDirectory.getAbsolutePath();
			}
		} catch (IllegalArgumentException e) {
			SelectRootPathUtils.showInvalidDirectoryChooserEntryInformation();
			return pathAsString;
		}
		return pathAsString;

	}
}

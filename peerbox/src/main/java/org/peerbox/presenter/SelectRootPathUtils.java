package org.peerbox.presenter;

import java.io.File;
import java.io.IOException;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.peerbox.UserConfig;
import org.peerbox.model.H2HManager;

import com.google.inject.Inject;

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

	public static boolean verifyRootPath(H2HManager h2hManager, UserConfig userConfig, String desiredRootPath) {
		try {
			File path = new File(desiredRootPath);
			Action createDirAction = Dialog.Actions.YES;
			if (!path.exists()) {
				createDirAction = SelectRootPathUtils.askForDirectoryCreation();
			}

			// TODO rootpath should be read from H2HManager!
			boolean isDirCreated = false;
			if (createDirAction.equals(Dialog.Actions.YES)) {
				isDirCreated = h2hManager.initializeRootDirectory(desiredRootPath);
				if (isDirCreated) {
					userConfig.setRootPath(desiredRootPath); // save path in property file
					// FIXME: this needs to be handled differently because loading the full view again
					// resets all the input of the user
					// (e.g. user enters all details and path is wrong -> need to enter everything again)
					// MainNavigator.navigate("/org/peerbox/view/LoginView.fxml");
					return true;
				} else {
					SelectRootPathUtils.showPermissionWarning();
				}
			}
		} catch (IOException e) {
			SelectRootPathUtils.showIncorrectSelectionInformation();
		}
		return false;
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

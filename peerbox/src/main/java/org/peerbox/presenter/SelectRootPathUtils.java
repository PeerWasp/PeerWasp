package org.peerbox.presenter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.peerbox.UserConfig;

public class SelectRootPathUtils {

	public static Action moveDirectoryConfirm(File newPath) {
		return Dialogs.create()
			      .title("Move Path")
			      .message( String.format(
			    		  "This will move the directory to a new location: %s.", newPath.toString()))
	    		  .actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
			      .showConfirm();
	}
	
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

	public static boolean verifyRootPath(UserConfig userConfig, String desiredRootPath) {
		try {
			File path = new File(desiredRootPath);
			Action createDirAction = Dialog.Actions.YES;
			if (!path.exists()) {
				createDirAction = SelectRootPathUtils.askForDirectoryCreation();
			}

			// TODO rootpath should be read from H2HManager!
			boolean isDirCreated = false;
			if (createDirAction.equals(Dialog.Actions.YES)) {
				isDirCreated = initializeRootDirectory(desiredRootPath);
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
	
	
	/**
	 * Sets the root directory path to the provided parameter. If the directory does not exist,
	 * it is created on the fly.
	 * @param rootDirectoryPath contains the absolute path to the root directory.
	 * @throws IOException if the provided rootDirectoryPath leads to a real file.
	 * @return true if the selected directory is valid and can be used. 
	 * @return false if either the parent directory does not exist or the user does not have write permissions.
	 */
	public static boolean initializeRootDirectory(String rootDirectoryPath) throws IOException {
		File rootDirectoryFile = new File(rootDirectoryPath);
		boolean initializedSuccessfull = true;
		if (rootDirectoryFile.exists()) {
			if (!rootDirectoryFile.isDirectory()) {
				throw new IOException("The provided path leads to a file, not a directory.");
			}
			if (!Files.isWritable(rootDirectoryFile.toPath())) {
				initializedSuccessfull = false;
			}

		} else {
			// check if parent directory exist and is writable
			File parentDirectory = rootDirectoryFile.getParentFile();
			if (parentDirectory == null || !Files.isWritable(parentDirectory.toPath())) {
				return false;
			}
			// create the directory, only set rootDirectory if successful
			initializedSuccessfull = rootDirectoryFile.mkdir();
		}

		return initializedSuccessfull;
	}
}

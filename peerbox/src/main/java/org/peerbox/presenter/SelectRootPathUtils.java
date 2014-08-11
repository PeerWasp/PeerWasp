package org.peerbox.presenter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

public class SelectRootPathUtils {

	public static Action confirmMoveDirectoryDialog(File newPath) {
		return Dialogs
				.create()
				.title("Move Path")
				.message(String.format("This will move the directory to a new location: %s.",
								newPath.toString()))
				.actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
				.showConfirm();
	}
	
	public static void showPermissionWarning() {
		Dialogs.create()
				.title("Directory creation failed")
				.message("Either the selected directory's parent directory does not exist "+
							"or you do not have permissions to create the directory.")
				.showWarning();
	}

	public static Action askForDirectoryCreation() {
		return Dialogs.create()
			      .title("Directory does not exist.")
			      .actions(Dialog.Actions.YES, Dialog.Actions.NO)
			      .message( "Create this directory?")
			      .showConfirm();
	}

	public static void showIncorrectSelectionInformation() {
		Dialogs.create().title("File instead of directory selected.")
		.message("Please change the path as it does not lead to a directory")
		.showInformation();
	}

	public static boolean verifyRootPath(String desiredRootPath) {
		try {
			File path = new File(desiredRootPath);
			Action createDirAction = Dialog.Actions.YES;
			if (!path.exists()) {
				createDirAction = SelectRootPathUtils.askForDirectoryCreation();
			}
			boolean isDirCreated = false;
			if (createDirAction.equals(Dialog.Actions.YES)) {
				isDirCreated = initializeRootDirectory(desiredRootPath);
				if (isDirCreated) {
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

	public static String showDirectoryChooser(String pathAsString, Window toOpenDialog) {
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
		File rootDirectory = new File(rootDirectoryPath);
		boolean success = true;
		if (rootDirectory.exists()) {
			if (!rootDirectory.isDirectory()) {
				throw new IOException("The provided path leads to a file, not a directory.");
			}
			if (!Files.isWritable(rootDirectory.toPath())) {
				success = false;
			}
		} else {
			// check if parent directory exist and is writable
			File parentDirectory = rootDirectory.getParentFile();
			if (parentDirectory == null || !Files.isWritable(parentDirectory.toPath())) {
				return false;
			}
			// create the directory, only set rootDirectory if successful
			success = rootDirectory.mkdir();
		}
		return success;
	}
}

package org.peerbox.presenter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class SelectRootPathUtils {

	public static boolean confirmMoveDirectoryDialog(File newPath) {
		boolean yes = false;
		
		Alert dlg = new Alert(AlertType.CONFIRMATION);
		dlg.setTitle("Move Directory");
		dlg.setHeaderText("Move the directory?");
		dlg.setContentText(String.format("This will move the directory to a new location: %s.",
								newPath.toString()));
		
		dlg.getButtonTypes().clear();
		dlg.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
		
		dlg.showAndWait();
		yes = dlg.getResult() == ButtonType.YES;
		
		return yes;
	}
	
	public static void showPermissionWarning() {
		Alert dlg = new Alert(AlertType.ERROR);
		dlg.setTitle("Error");
		dlg.setHeaderText("Cannot create directory");
		dlg.setContentText("Either the selected directory's parent directory does not exist " + 
				"or you do not have permissions to create the directory.");
		
		dlg.showAndWait();
	}

	public static boolean askForDirectoryCreation() {
		
		boolean yes = false;
		
		Alert dlg = new Alert(AlertType.CONFIRMATION);
		dlg.setTitle("Create Directory");
		dlg.setHeaderText("Create the directory?");
		dlg.setContentText("The directory does not exist yet.");
		dlg.getButtonTypes().clear();
		dlg.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
		
		dlg.showAndWait();
		yes = dlg.getResult() == ButtonType.YES;
		
		return yes;
	}

	public static void showIncorrectSelectionInformation() {
		Alert dlg = new Alert(AlertType.INFORMATION);
		dlg.setTitle("File");
		dlg.setHeaderText("File instead of directory selected");
		dlg.setContentText("Please select a directory.");
		dlg.showAndWait();
	}

	public static boolean verifyRootPath(String desiredRootPath) {
		try {
			File path = new File(desiredRootPath);
			boolean createDirAction = true;
			if (!path.exists()) {
				createDirAction = SelectRootPathUtils.askForDirectoryCreation();
			}
			boolean isDirCreated = false;
			if (createDirAction) {
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
		Alert dlg = new Alert(AlertType.ERROR);
		dlg.setTitle("Error");
		dlg.setHeaderText("Cannot open the directory");
		dlg.setContentText("The selected directory and its parent directory do not exist.");
		dlg.showAndWait();
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
	
	public static boolean isValidRootPath(Path path) {
		return path != null 
				&& Files.exists(path) 
				&& Files.isDirectory(path) 
				&& Files.isWritable(path);
	}
}

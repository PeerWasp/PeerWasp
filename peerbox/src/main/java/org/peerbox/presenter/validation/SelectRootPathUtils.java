package org.peerbox.presenter.validation;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.apache.commons.io.FileUtils;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.utils.AlertUtils;

public class SelectRootPathUtils {

	public static boolean confirmMoveDirectoryDialog(File newPath) {
		boolean yes = false;

		Alert dlg = AlertUtils.create(AlertType.CONFIRMATION);
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


	public static ValidationResult validateRootPath(Path rootPath) {
		// check whether dir exists and create it if desired
		if (!Files.exists(rootPath)) {
			boolean createDir = SelectRootPathUtils.askToCreateDirectory();
			if (createDir) {
				ValidationResult res = createRootPath(rootPath);
				if(res.isError()) {
					return res;
				}
			} else {
				// do not create but return
				return ValidationResult.ROOTPATH_NOTEXISTS;
			}
		}

		// path exists -- is it a directory?
		if(!Files.isDirectory(rootPath, LinkOption.NOFOLLOW_LINKS)) {
			return ValidationResult.ROOTPATH_NOTADIRECTORY;
		}

		// check write permissions
		if(!Files.isWritable(rootPath)) {
			return ValidationResult.ROOTPATH_NOTWRITABLE;
		}

		return ValidationResult.OK;
	}

	private static boolean askToCreateDirectory() {

		boolean yes = false;

		Alert dlg = AlertUtils.create(AlertType.CONFIRMATION);
		dlg.setTitle("Create Directory");
		dlg.setHeaderText("Create the directory?");
		dlg.setContentText("The directory does not exist yet. Do you want to create it?");
		dlg.getButtonTypes().clear();
		dlg.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		dlg.showAndWait();
		yes = dlg.getResult() == ButtonType.YES;

		return yes;
	}

	private static ValidationResult createRootPath(Path path) {
		try {
			Files.createDirectories(path);
		} catch (AccessDeniedException ae) {
			return ValidationResult.ROOTPATH_CREATE_ACCESSDENIED;
		} catch (IOException e) {
			return ValidationResult.ROOTPATH_CREATE_FAILED;
		}

		if (!Files.exists(path)) {
			return ValidationResult.ROOTPATH_CREATE_FAILED;
		}

		return ValidationResult.OK;
	}

	public static String showDirectoryChooser(String pathAsString, Window toOpenDialog) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose your root directory");

		// if parent of given directory does exist, we open chooser with that location.
		// otherwise, take the user home as fallback.
		File path = new File(pathAsString).getParentFile();
		if(path.getParentFile().exists()) {
			chooser.setInitialDirectory(path.getParentFile());
		} else {
			chooser.setInitialDirectory(FileUtils.getUserDirectory());
		}

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

	private static void showInvalidDirectoryChooserEntryInformation() {
		Alert dlg = AlertUtils.create(AlertType.ERROR);
		dlg.setTitle("Error");
		dlg.setHeaderText("Cannot open the directory");
		dlg.setContentText("The selected directory or its parent directory does not exist.");
		dlg.showAndWait();
	}

	public static boolean isValidRootPath(Path path) {
		return path != null
				&& Files.exists(path)
				&& Files.isDirectory(path)
				&& Files.isWritable(path);
	}
}

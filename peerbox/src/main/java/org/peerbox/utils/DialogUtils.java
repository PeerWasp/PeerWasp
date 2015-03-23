package org.peerbox.utils;

import java.util.Collection;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Utilities for JavaFX dialogs.
 *
 * @author albrecht
 *
 */
public final class DialogUtils {

	private DialogUtils() {
		// prevent instance
	}

	/**
	 * Creates an Alert dialog of given alert type.
	 * In addition, the dialog is decorated and configured (icons, ...).
	 *
	 * @param type
	 * @return alert dialog
	 */
	public static Alert createAlert(AlertType type) {
		Alert dlg = new Alert(type);
		decorateDialogWithIcon(dlg);

		if (OsUtils.isLinux()) {
			/*
			 * FIXME: Due to a bug in JavaFX and Java for linux (glass), the dialogs are not
			 * represented correctly. The size does not increase with the text length and is
			 * cut off if the text is too long (no wrapping).
			 * see the following links:
			 * - https://stackoverflow.com/questions/28937392/javafx-alerts-and-their-size
			 * - https://javafx-jira.kenai.com/browse/RT-40230
			 * Temporary solution: we make dialogs resizable and set a preferred size on Linux.
			 */
			dlg.setResizable(true);
			dlg.getDialogPane().setPrefSize(420, 280);
		}

		return dlg;
	}

	/**
	 * Decorates a dialog with window icons.
	 * Note: this may not be required anymore with newer Java versions.
	 *
	 * @param dlg the dialog to decorate
	 */
	public static void decorateDialogWithIcon(Dialog<?> dlg) {
		Window window = dlg.getDialogPane().getScene().getWindow();
		if (window instanceof Stage) {
			Stage stage = (Stage) dlg.getDialogPane().getScene().getWindow();
			Collection<Image> icons = IconUtils.createWindowIcons();
			stage.getIcons().addAll(icons);
		}
	}

}

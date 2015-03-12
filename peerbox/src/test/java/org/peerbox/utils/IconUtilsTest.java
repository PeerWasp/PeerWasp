package org.peerbox.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.junit.Rule;
import org.junit.Test;
import org.peerbox.helper.JavaFXThreadingRule;

public class IconUtilsTest {

	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

	@Test
	public void testCreateWindowIcons() {
		List<Image> icons = IconUtils.createWindowIcons();
		assertFalse(icons.isEmpty());
	}

	@Test
	public void testDecorateDialogWithIcon() {
		Alert dlg = new Alert(AlertType.ERROR);
		Stage stage = (Stage) dlg.getDialogPane().getScene().getWindow();

		assertTrue(stage.getIcons().isEmpty());
		IconUtils.decorateDialogWithIcon(dlg);
		assertFalse(stage.getIcons().isEmpty());
	}

	@Test
	public void testCreateErrorIcon() {
		Node icon = IconUtils.createErrorIcon();
		assertNotNull(icon);
	}

}

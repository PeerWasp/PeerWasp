package org.peerbox.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.junit.Rule;
import org.junit.Test;
import org.peerbox.helper.JavaFXThreadingRule;

public class DialogUtilsTest {

	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

	@Test
		public void testCreateAlert() {
			Alert dlg = DialogUtils.createAlert(AlertType.WARNING);
			assertNotNull(dlg);
			assertEquals(AlertType.WARNING, dlg.getAlertType());
			assertTrue(alertHasIcons(dlg));
	
			Alert dlg_info = DialogUtils.createAlert(AlertType.INFORMATION);
			assertNotNull(dlg_info);
			assertEquals(AlertType.INFORMATION, dlg_info.getAlertType());
			assertTrue(alertHasIcons(dlg));
		}

	@Test
	public void testDecorateDialogWithIcon() {
		Alert dlg = new Alert(AlertType.ERROR);
		assertFalse(alertHasIcons(dlg));
		DialogUtils.decorateDialogWithIcon(dlg);
		assertTrue(alertHasIcons(dlg));
	}

	private boolean alertHasIcons(Alert dlg) {
		ObservableList<Image> icons = alertIcons(dlg);
		return !icons.isEmpty();
	}

	private ObservableList<Image> alertIcons(Alert dlg) {
		Stage stage = (Stage) dlg.getDialogPane().getScene().getWindow();
		ObservableList<Image> icons = stage.getIcons();
		return icons;
	}

}

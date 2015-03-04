package org.peerbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Utility class for application icons.
 *
 * @author albrecht
 *
 */
public class IconUtils {

	private IconUtils() {
		// prevent instance
	}

	/**
	 * Creates a list of icons that can be used to decorate application windows, e.g. in the tray or in
	 * the top window bar. The collection contains icons in different sizes.
	 *
	 * @return list of icons in increasing size.
	 */
	public static List<Image> createWindowIcons() {
		List<Image> icons = new ArrayList<>();
		icons.add(new Image(IconUtils.class.getResourceAsStream("/images/peerwasp-icon-16x16.png")));
		icons.add(new Image(IconUtils.class.getResourceAsStream("/images/peerwasp-icon-32x32.png")));
		icons.add(new Image(IconUtils.class.getResourceAsStream("/images/peerwasp-icon-48x48.png")));
		icons.add(new Image(IconUtils.class.getResourceAsStream("/images/peerwasp-icon-64x64.png")));
		return icons;
	}


	/**
	 * Decorates an Alert dialog with window icons.
	 * Note: this may not be required anymore with newer Java versions.
	 *
	 * @param dlg the Alert dialog to decorate
	 */
	public static void decorateDialogWithIcon(Alert dlg) {
		Window window = dlg.getDialogPane().getScene().getWindow();
		if (window instanceof Stage) {
			Stage stage = (Stage) dlg.getDialogPane().getScene().getWindow();
			Collection<Image> icons = createWindowIcons();
			stage.getIcons().addAll(icons);
		}
	}

	public static Node createErrorIcon() {
		GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
		Glyph graphic = fontAwesome.create(FontAwesome.Glyph.EXCLAMATION_TRIANGLE);
		graphic.setFontSize(20.0);
		graphic.setColor(Color.RED);
		return graphic;
	}
}

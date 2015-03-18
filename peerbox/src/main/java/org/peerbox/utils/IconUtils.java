package org.peerbox.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

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
	 * Returns an error icon (glyph created using font awesome).
	 *
	 * @return graphic node
	 */
	public static Node createErrorIcon() {
		GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
		Glyph graphic = fontAwesome.create(FontAwesome.Glyph.EXCLAMATION_TRIANGLE);
		graphic.setFontSize(20.0);
		graphic.setColor(Color.RED);
		return graphic;
	}

	public static void initFontAwesomeOffline() {
		FontAwesomeOffline.init();
	}

	public static GlyphFont getFontAwesome() {
		return FontAwesomeOffline.getGlyphFont();
	}


	/**
	 * This is a wrapper class that registers the font awesome .ttf included in the package.
	 * ControlsFX loads this font dynamically at run time from a CDN. Thus, it works only if Internet
	 * connection is available
	 * .
	 * This class registers an included version of the font. The {@link #init()} method should be called
	 * during the initialization procedure of the application such that the font is available early.
	 *
	 * @author albrecht
	 *
	 */
	private static class FontAwesomeOffline {

		private static final String fontLocation = "/fonts/fontawesome-webfont.ttf";
		private static GlyphFont font;

		private FontAwesomeOffline() {
			// prevent instances
		}

		public static void init() {
			InputStream input = FontAwesomeOffline.class.getResourceAsStream(fontLocation);
			font = new FontAwesome(input);
			GlyphFontRegistry.register(font);
		}

		public static GlyphFont getGlyphFont() {
			return font;
		}

	}

}

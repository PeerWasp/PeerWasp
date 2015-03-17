package org.peerbox.view;

import java.io.InputStream;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

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
public class FontAwesomeOffline {

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

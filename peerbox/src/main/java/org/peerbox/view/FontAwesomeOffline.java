package org.peerbox.view;

import java.io.InputStream;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

/**
 * This is a wrapper class that registers the font awesome .ttf included in the package.
 * ControlsFX loads this font dynamically at run time from a CDN. Thus, it works only if internet
 * connectoin is available.
 * This class registers an included version of the font in a static block (thus, it is mandatory
 * to reference this class somewhere, e.g. by calling the init() method).
 *
 * @author albrecht
 *
 */
public class FontAwesomeOffline {

	private static final GlyphFont font;

	static {
		InputStream input = FontAwesomeOffline.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf");
		font = new FontAwesome(input);
		GlyphFontRegistry.register(font);
	}

	private FontAwesomeOffline() {

	}

	public static void init() {
		// nothing to do
	}

	public static GlyphFont getGlyphFont() {
		return font;
	}

}

package org.peerbox.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.junit.Test;

public class FontAwesomeOfflineTest {

	@Test
	public void testGetGlyphFont() {
		assertNull(FontAwesomeOffline.getGlyphFont());

		FontAwesomeOffline.init();
		GlyphFont font = FontAwesomeOffline.getGlyphFont();
		assertNotNull(font);

		GlyphFont registeredFont = GlyphFontRegistry.font("FontAwesome");
		assertNotNull(registeredFont);
		assertEquals(font, registeredFont);
	}

}

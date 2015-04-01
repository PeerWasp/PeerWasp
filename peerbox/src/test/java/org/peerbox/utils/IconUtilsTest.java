package org.peerbox.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.image.Image;

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
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
	public void testCreateErrorIcon() {
		Node icon = IconUtils.createErrorIcon();
		assertNotNull(icon);
	}

	@Test
	public void testInitFontAwesomeOffline() {
		// this test may fail if already initialized due to other tests.
		assertNull(IconUtils.getFontAwesome());

		IconUtils.initFontAwesomeOffline();
		GlyphFont font = IconUtils.getFontAwesome();
		assertNotNull(font);

		GlyphFont registeredFont = GlyphFontRegistry.font("FontAwesome");
		assertNotNull(registeredFont);
		assertEquals(font, registeredFont);
	}

}

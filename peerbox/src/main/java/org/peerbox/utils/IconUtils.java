package org.peerbox.utils;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

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
}

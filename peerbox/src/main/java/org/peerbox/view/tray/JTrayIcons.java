package org.peerbox.view.tray;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

class JTrayIcons {
	private static final String DEFAULT_ICON = "/images/icon.png";
	private static final String SYNCING_ICON = "/images/icon_sync.png";
	// TODO: add more icons here, e.g. when error icon, sync icon, ... ->

	// cache the image instances.
	private Image defaultIcon;
	private Image syncingIcon;

	public Image getDefaultIcon() throws IOException {
		if (defaultIcon == null) {
			defaultIcon = ImageIO.read(getClass().getResource(DEFAULT_ICON));
		}
		return defaultIcon;
	}
	
	public Image getSyncingIcon() throws IOException {
		if (syncingIcon == null) {
			syncingIcon = ImageIO.read(getClass().getResource(SYNCING_ICON));
		}
		return syncingIcon;
	}
}
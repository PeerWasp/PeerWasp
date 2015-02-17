package org.peerbox.view.tray;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

class JTrayIcons {
	private static final String DEFAULT_ICON = "/images/trayicon.png";
	private static final String SUCCESS_ICON = "/images/trayicon_success.png";
	private static final String SYNCING_ICON = "/images/trayicon_sync.png";
	private static final String ERROR_ICON = "/images/trayicon_error.png";
	// TODO: add more icons here, e.g. when error icon, sync icon, ... ->

	// cache the image instances.
	private Image defaultIcon;
	private Image syncingIcon;
	private Image successIcon;

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
	
	public Image getSuccessIcon() throws IOException{
		if (successIcon == null) {
			successIcon = ImageIO.read(getClass().getResource(SUCCESS_ICON));
		}
		return successIcon;
	}
}
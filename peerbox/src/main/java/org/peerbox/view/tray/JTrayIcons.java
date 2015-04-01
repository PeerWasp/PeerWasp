package org.peerbox.view.tray;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Responsible for loading and caching the various tray icons.
 * Each icon is loaded and scaled with the first getXIcon method call. Further calls return the
 * already created instance.
 *
 * @author albrecht
 *
 */
class JTrayIcons {
	// icon resources
	private static final String DEFAULT_ICON = "/images/trayicon.png";
	private static final String SUCCESS_ICON = "/images/trayicon_success.png";
	private static final String SYNCING_ICON = "/images/trayicon_sync.png";
	private static final String ERROR_ICON = "/images/trayicon_error.png";
	// add more icons here, e.g. when error icon, sync icon, ... ->

	// size of the tray icons (as read from SystemTray)
	private final Dimension traySize;

	// cached image instances.
	private Image defaultIcon;
	private Image syncingIcon;
	private Image successIcon;
	private Image errorIcon;

	public JTrayIcons() {
		this.traySize = new Dimension();
		initSize();
	}

	/**
	 * gets the tray size for the icons
	 */
	private void initSize() {
		Dimension trayDim = java.awt.SystemTray.getSystemTray().getTrayIconSize();
		if (trayDim == null || trayDim.getWidth() < 1.0 || trayDim.getWidth() < 1.0) {
			traySize.setSize(24.0, 24.0);
		} else {
			traySize.setSize(trayDim);
		}
	}

	/**
	 * Resize the image to the size of the tray using traySize.width
	 *
	 * @param icon to scale
	 * @return scaled instance. null if provided icon is null.
	 */
	private Image resize(BufferedImage icon) {
		if (icon != null) {
			int width = (int) traySize.getWidth();
			Image resizedIcon = icon.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
			return resizedIcon;
		}
		return null;
	}

	public Image getDefaultIcon() throws IOException {
		if (defaultIcon == null) {
			BufferedImage icon = ImageIO.read(getClass().getResource(DEFAULT_ICON));
			defaultIcon = resize(icon);
		}
		return defaultIcon;
	}

	public Image getSyncingIcon() throws IOException {
		if (syncingIcon == null) {
			BufferedImage icon = ImageIO.read(getClass().getResource(SYNCING_ICON));
			syncingIcon = resize(icon);
		}
		return syncingIcon;
	}

	public Image getSuccessIcon() throws IOException{
		if (successIcon == null) {
			BufferedImage icon = ImageIO.read(getClass().getResource(SUCCESS_ICON));
			successIcon = resize(icon);
		}
		return successIcon;
	}

	public Image getErrorIcon() throws IOException{
		if(errorIcon == null){
			BufferedImage icon = ImageIO.read(getClass().getResource(ERROR_ICON));
			errorIcon = resize(icon);
		}
		return errorIcon;
	}
}
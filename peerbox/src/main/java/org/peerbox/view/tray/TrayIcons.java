package org.peerbox.view.tray;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TrayIcons {
	private static final String DEFAULT_ICON = "/images/icon.png";
	// TODO: add more icons here, e.g. when error icon, sync icon, ... -> 
	
	private Image defaultIcon;
	
	public Image getDefaultIcon() throws IOException {
		if(defaultIcon == null) {
			defaultIcon = ImageIO.read(getClass().getResource(DEFAULT_ICON));
		}
		return defaultIcon;
	}
}

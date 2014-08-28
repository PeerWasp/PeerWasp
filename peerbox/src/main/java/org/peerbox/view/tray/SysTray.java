package org.peerbox.view.tray;

import java.awt.AWTException;
import java.awt.TrayIcon;
import java.io.IOException;

import com.google.inject.Inject;

public class SysTray {
	
	private String tooltip;
	private java.awt.TrayIcon trayIcon;
	private TrayIcons icons;
	private TrayMenu menu;

	@Inject 
	public SysTray(TrayMenu menu, TrayIcons icons) {
		this.icons = icons;
		this.menu = menu;
		setTooltip(tooltip);
	}
	
	public String getTooltip() {
		return tooltip;
	}
	
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
		if(trayIcon != null) {
			trayIcon.setToolTip(this.tooltip);
		}
	}
	
	public void addToSystemTray() throws AWTException, IOException {
		trayIcon = create();
		java.awt.SystemTray sysTray = java.awt.SystemTray.getSystemTray();
		sysTray.add(trayIcon);
	}

	private TrayIcon create() throws IOException {
		TrayIcon tray = new java.awt.TrayIcon(icons.getDefaultIcon());
		tray.setImageAutoSize(true);
		tray.setToolTip(tooltip);
		tray.setPopupMenu(menu.create());
		return tray;
	}

}

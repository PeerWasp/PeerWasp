package org.peerbox.view.tray;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;

import javafx.application.Platform;


public class SysTray {
	
	private String tooltip;
	private TrayIcon trayIcon;
	private TrayIcons icons;
	private TrayMenu menu;

	public SysTray() {
		icons = new TrayIcons();
		menu = new TrayMenu();
		tooltip = "PeerBox";
	}
	
	public String getTooltip() {
		return tooltip;
	}
	
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
		// TODO: maybe update tray here -> we could show some info in the tooltip,
		// e.g. up to date, synchronizing, offline, ...
	}
	
	public void addToTray() throws AWTException, IOException {
		if (SystemTray.isSupported()) {
			Platform.setImplicitExit(false);
			SystemTray sysTray = SystemTray.getSystemTray();
			trayIcon = new TrayIcon(icons.getDefaultIcon());
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip(getTooltip());
			trayIcon.setPopupMenu(menu.create());
			sysTray.add(trayIcon);
		}
	}
	
	
}

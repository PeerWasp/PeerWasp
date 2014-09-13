package org.peerbox.view.tray;

import org.peerbox.presenter.tray.TrayActionHandler;
import org.peerbox.presenter.tray.TrayException;


public abstract class AbstractSystemTray {

	protected TrayActionHandler trayActionHandler;
	
	public AbstractSystemTray(TrayActionHandler actionHandler) {
		trayActionHandler = actionHandler;
	}
	
	public abstract void show() throws TrayException;

	public abstract void setTooltip(String tooltip);

	public abstract void showDefaultIcon() throws TrayException;

	public abstract void showSyncingIcon() throws TrayException;
	
	public void showInformationMessage(String title, String message) {}

}

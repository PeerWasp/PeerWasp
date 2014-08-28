package org.peerbox.presenter.tray;

import java.awt.MenuItem;

public abstract class AbstractMenu {

	protected MenuItem menu;
	
	public AbstractMenu() {
		menu = new MenuItem();
		initialize();
	}
	
	protected abstract void initialize();
	
	public MenuItem getMenuItem() {
		return menu;
	}
}

package org.peerbox.view.tray;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;

import org.peerbox.presenter.tray.TrayActionHandler;

final class JTrayMenu {

	private PopupMenu root;
	private TrayActionHandler actionHandler;

	public JTrayMenu(TrayActionHandler actionHandler) {
		this.actionHandler = actionHandler;
	}

	public PopupMenu create(boolean isUserLoggedIn) {
		root = new PopupMenu();

		if (isUserLoggedIn) {
			root.add(createRootFolderMenu());
			// root.add(createRecentFilesMenu()); // TODO implement it...
			root.addSeparator();
			root.add(createSettingsMenu());
			root.addSeparator();
		}
		root.add(createActivityMenu());
		root.add(createQuitMenu());

		return root;
	}

	private MenuItem createRootFolderMenu() {
		MenuItem rootItem = new MenuItem("Open Folder");
		rootItem.addActionListener(createRootFolderListener());
		return rootItem;
	}

	private ActionListener createRootFolderListener() {
		ActionListener closeListener = new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent event) {
				actionHandler.openRootFolder();
			}
		};
		return closeListener;
	}

	private MenuItem createSettingsMenu() {
		MenuItem settingsItem = new MenuItem("Settings");
		settingsItem.addActionListener(createSettingsListener());
		return settingsItem;
	}

	public ActionListener createSettingsListener() {
		ActionListener settingsListener = new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				actionHandler.showSettings();
			}
		};
		return settingsListener;
	}

	private MenuItem createActivityMenu() {
		MenuItem activityItem = new MenuItem("Activity");
		activityItem.addActionListener(createActivityListener());
		return activityItem;
	}

	private ActionListener createActivityListener() {
		ActionListener activityListener = new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				actionHandler.showActivity();
			}
		};
		return activityListener;
	}

	private MenuItem createQuitMenu() {
		MenuItem closeItem = new MenuItem("Quit");
		closeItem.addActionListener(createQuitListener());
		return closeItem;
	}

	private ActionListener createQuitListener() {
		ActionListener closeListener = new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				actionHandler.quit();
			}
		};
		return closeListener;
	}
}

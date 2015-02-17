package org.peerbox.view.tray;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.peerbox.presenter.tray.TrayActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JTrayMenu {

	private static final Logger logger = LoggerFactory.getLogger(JTrayMenu.class);

	private PopupMenu root;
	private TrayActionHandler actionHandler;

	public JTrayMenu(TrayActionHandler actionHandler) {
		this.actionHandler = actionHandler;
	}

	public PopupMenu create() {
		root = new PopupMenu();

		root.add(createRootFolderMenu());
		// root.add(createRecentFilesMenu()); // TODO implement it...
		root.addSeparator();
		root.add(createSettingsMenu());
		root.addSeparator();
		root.add(createActivityMenu());
		root.add(createQuitMenu());

		return root;
	}

	public TrayActionHandler getTrayActionHandler(){
		return actionHandler;
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
				try {
					actionHandler.openRootFolder();
				} catch (IOException ex) {
					logger.debug("Could not open folder.", ex);
					logger.error("Could not open root folder.");
				}
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

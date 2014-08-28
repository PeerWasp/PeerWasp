package org.peerbox.view.tray;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;

import org.peerbox.presenter.tray.RecentFilesMenu;
import org.peerbox.presenter.tray.RootFolderMenu;
import org.peerbox.presenter.tray.SettingsMenu;

import com.google.inject.Inject;


public class TrayMenu {
	
	private PopupMenu root;
	
	@Inject
	private RootFolderMenu rootFolder;
	@Inject 
	private RecentFilesMenu recentFiles;
	@Inject
	private SettingsMenu settings;
	
	
	public PopupMenu create() {
		root = new PopupMenu();
		
		root.add(rootFolder.getMenuItem());
		root.add(recentFiles.getMenuItem());
        root.addSeparator();
        root.add(settings.getMenuItem());
        root.addSeparator();
        root.add(createCloseMenu());
           
        return root;
	}
	
	
	private MenuItem createCloseMenu() {
	   MenuItem closeItem = new MenuItem("Quit");
        closeItem.addActionListener(createCloseListener());
		return closeItem;
	}

	private ActionListener createCloseListener() {
		ActionListener closeListener = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	// TODO: proper exit handling. should not be done here, 
            	// but rather within the application somewhere (graceful disconnect etc)
                System.exit(0);
            }
        };
        return closeListener;
	}

}

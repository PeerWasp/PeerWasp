package org.peerbox.view.tray;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;


public class TrayMenu {

	
	private PopupMenu root;
	
	public PopupMenu create() {
		root = new PopupMenu();
		
		root.add(createOpenFolderMenu());
		root.add(createFilesMenu());
        root.addSeparator();
        root.add(createSettingsMenu());
        root.addSeparator();
        root.add(createCloseMenu());
           
        return root;
	}
	
	
	private MenuItem createCloseMenu() {
	   MenuItem closeItem = new MenuItem("Quit");
        closeItem.addActionListener(createCloseListener());
		return closeItem;
	}


	private MenuItem createSettingsMenu() {
		MenuItem settings = new MenuItem("Settings");
		return settings;
	}


	private MenuItem createFilesMenu() {
		Menu files = new Menu("Recent Changes");
		
		// sample file.
		MenuItem aFile = new MenuItem("myfile.txt");
		aFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.Desktop.getDesktop().open(new File("myfile.txt"));
				} catch (IOException e1) {
					System.err.println("Could not open the file");
				}
			}
		});
		
		files.add(aFile); 
        // TODO: add a way to add menu items to the files menu such that we can insert recently synced files
        return files;
	}


	private MenuItem createOpenFolderMenu() {
		MenuItem open = new MenuItem("Open Folder");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.Desktop.getDesktop().open(new File("bla")); // folder
				} catch (IOException e1) {
					System.err.println("Could not open the folder");
				}
			}
		});
		// TODO: add listener
		return open;
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

package org.peerbox.presenter.tray;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class RecentFilesMenu extends AbstractMenu {

	
	@Override
	protected void initialize() {
		menu  = new Menu("Recent Changes");
		
		// TODO: implement nice way to add new files etc. on the fly and update the menu.
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
		
		((Menu)menu).add(aFile); 
        // TODO: add a way to add menu items to the files menu such that we can insert recently synced files
	}
}

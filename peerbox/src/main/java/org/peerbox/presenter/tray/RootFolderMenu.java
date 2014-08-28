package org.peerbox.presenter.tray;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.apache.commons.io.FileUtils;

public class RootFolderMenu extends AbstractMenu {
	
	@Override
	protected void initialize() {
		menu.setLabel("Open Folder");
		menu.addActionListener(new RootFolderAction()); 
	}
	
	private class RootFolderAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		public RootFolderAction() {
			super("Open Folder");
			putValue(SHORT_DESCRIPTION, "Open the data folder.");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				// TODO: should be the root folder of the user of course...!
				java.awt.Desktop.getDesktop().open(new File(FileUtils.getUserDirectoryPath())); 
			} catch (IOException e1) {
				System.err.println("Could not open the folder");
			}
		}
	}

}

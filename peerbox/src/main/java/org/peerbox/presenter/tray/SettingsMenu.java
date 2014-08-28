package org.peerbox.presenter.tray;

import java.awt.event.ActionEvent;

import javafx.application.Platform;

import javax.swing.AbstractAction;

import org.peerbox.SettingsStage;

import com.google.inject.Inject;

public class SettingsMenu extends AbstractMenu {

	@Inject 
	private SettingsStage settingsStage;
	
	@Override
	protected void initialize() {
		menu.setLabel("Settings");
		menu.addActionListener(new SettingsAction()); 
	}
	
	private class SettingsAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		public SettingsAction() {
			super("Settings");
			putValue(SHORT_DESCRIPTION, "Open the settings dialog.");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Platform.runLater(() -> {
				settingsStage.show();
			});
		}
	}
	
}

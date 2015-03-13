package org.peerbox.presenter.settings;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class AboutPeerWasp implements Initializable {
	
	@FXML
    private void opengithub(ActionEvent event) throws URISyntaxException {
		URI github = new URI("https://github.com/Hive2Hive/PeerBox");
		SwingUtilities.invokeLater(() -> {
			try {
				java.awt.Desktop.getDesktop().browse(github);
			} catch (Exception e) {
			}
		});
    }
	
	@FXML
    private void openpeerwasp(ActionEvent event) throws URISyntaxException {
		URI peerwasp = new URI("http://www.peerwasp.com");
		SwingUtilities.invokeLater(() -> {
			try {
				java.awt.Desktop.getDesktop().browse(peerwasp);
			} catch (Exception e) {
			}
		});
    }
	
	@FXML
    private void mailtopeerwasp(ActionEvent event) throws URISyntaxException {
		URI peerwaspmail = new URI("mailto:info@peerwasp.com");
		SwingUtilities.invokeLater(() -> {
			try {
				java.awt.Desktop.getDesktop().browse(peerwaspmail);
			} catch (Exception e) {
			}
		});
    }
	
	@FXML
    private void openh2h(ActionEvent event) throws URISyntaxException {
		URI hive2hive = new URI("http://www.hive2hive.com");
		SwingUtilities.invokeLater(() -> {
			try {
				java.awt.Desktop.getDesktop().browse(hive2hive);
			} catch (Exception e) {
			}
		});
    }
	
	@FXML
    private void opentomp2p(ActionEvent event) throws URISyntaxException {
		URI tomp2p = new URI("http://tomp2p.net/");
		SwingUtilities.invokeLater(() -> {
			try {
				java.awt.Desktop.getDesktop().browse(tomp2p);
			} catch (Exception e) {
			}
		});
    }



	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

}

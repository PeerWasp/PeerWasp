package org.peerbox.presenter.settings;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import org.peerbox.utils.BrowserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AboutPeerWasp implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(AboutPeerWasp.class);

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	@FXML
    private void opengithub(ActionEvent event) {
		String github = "https://github.com/Hive2Hive/PeerBox";
		try {
			BrowserUtils.openURL(github);
		} catch (Exception e) {
			logger.warn("Could not open Github URL: '{}'", github, e);
		}
    }

	@FXML
    private void openpeerwasp(ActionEvent event) {
		String peerwasp = "http://www.peerwasp.com";
		try {
			BrowserUtils.openURL(peerwasp);
		} catch (Exception e) {
			logger.warn("Could not open PeerWasp URL: '{}'", peerwasp, e);
		}
    }

	@FXML
    private void mailtopeerwasp(ActionEvent event) {
		String peerwaspmail = "mailto:info@peerwasp.com";
		try {
			BrowserUtils.openURL(peerwaspmail);
		} catch (Exception e) {
			logger.warn("Could not open PeerWasp Mail: '{}'", peerwaspmail, e);
		}
    }

	@FXML
    private void openh2h(ActionEvent event) {
		String hive2hive = "http://www.hive2hive.com";
		try {
			BrowserUtils.openURL(hive2hive);
		} catch (Exception e) {
			logger.warn("Could not open Hive2Hive URL: '{}'", hive2hive, e);
		}
    }

	@FXML
    private void opentomp2p(ActionEvent event) {
		String tomp2p = "http://tomp2p.net/";
		try {
			BrowserUtils.openURL(tomp2p);
		} catch (Exception e) {
			logger.warn("Could not open TomP2P URL: '{}'", tomp2p, e);
		}
    }

}

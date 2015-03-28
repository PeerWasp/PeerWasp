package org.peerbox.presenter.settings;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import org.peerbox.utils.BrowserUtils;
import org.peerbox.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class About implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(About.class);

	@FXML
	private Label lblPeerWaspVersion;
	@FXML
	private Label lblH2HVersion;
	@FXML
	private Label lblTomP2PVersion;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		updateVersions();
	}

	private void updateVersions() {
		final Runnable setVersions = new Runnable() {
			@Override
			public void run() {
				String peerWaspVersion = VersionUtils.getPeerWaspVersion();
				if (peerWaspVersion == null || peerWaspVersion.isEmpty()) {
					peerWaspVersion = "unknown";
				}
				lblPeerWaspVersion.setText(peerWaspVersion);

				String h2hVersion = VersionUtils.getH2HVersion();
				if (h2hVersion == null || h2hVersion.isEmpty()) {
					h2hVersion = "unknown";
				}
				lblH2HVersion.setText(h2hVersion);

				String tomp2pVersion = VersionUtils.getTomP2PVersion();
				if (tomp2pVersion == null || tomp2pVersion.isEmpty()) {
					tomp2pVersion = "unknown";
				}
				lblTomP2PVersion.setText(tomp2pVersion);
			}
		};

		if (Platform.isFxApplicationThread()) {
			setVersions.run();
		} else {
			Platform.runLater(setVersions);
		}
	}

	@FXML
    private void opengithub(ActionEvent event) {
		String github = "https://github.com/PeerWasp/PeerWasp";
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

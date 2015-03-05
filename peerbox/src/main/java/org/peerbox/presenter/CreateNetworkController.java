package org.peerbox.presenter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.utils.IconUtils;
import org.peerbox.view.ViewNames;
import org.peerbox.view.controls.ErrorLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class CreateNetworkController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(CreateNetworkController.class);

	private INodeManager nodeManager;
	private NavigationService fNavigationService;

	@FXML
	private Button btnCreate;

	@FXML
	private TextField txtIPAddress;

	@FXML
	private ErrorLabel lblError;

	@Inject
	public CreateNetworkController(NavigationService navigationService, INodeManager nodeManager) {
		this.fNavigationService = navigationService;
		this.nodeManager = nodeManager;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		String allIps = getAllIpAddresses();
		Platform.runLater(() -> {
			if (allIps != null) {
				txtIPAddress.setText(allIps);
			}
		});
	}

	private String getAllIpAddresses() {
		StringBuilder sb = new StringBuilder();
		try {
			// iterate through interface list and retrieve their addresses.
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces != null) {
				while (interfaces.hasMoreElements()) {
					NetworkInterface netInt = interfaces.nextElement();
					Enumeration<InetAddress> addresses = netInt.getInetAddresses();
					if (addresses != null) {
						while (addresses.hasMoreElements()) {
							InetAddress inetAddr = addresses.nextElement();
							// only consider IPv4 at the moment
							if (inetAddr instanceof Inet4Address) {
								String ip = inetAddr.getHostAddress();
								sb.append(ip).append(", ");
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			setError("Could not retrieving network interface list.");
		}

		sb.delete(sb.lastIndexOf(","), sb.length());
		return sb.toString();
	}

	@FXML
	public void navigateBackAction(ActionEvent event) {
		boolean goBack = true;
		clearError();
		if (nodeManager.isConnected()) {
			goBack = showConfirmDeleteNetworkDialog();
		}
		if (goBack) {
			nodeManager.leaveNetwork();
			btnCreate.setText("Create");
			logger.debug("Navigate back.");
			fNavigationService.navigateBack();
		}
	}

	private boolean showConfirmDeleteNetworkDialog() {
		boolean yes = false;

		Window owner = txtIPAddress.getScene().getWindow();
		Alert dlg = new Alert(AlertType.CONFIRMATION);
		IconUtils.decorateDialogWithIcon(dlg);
		dlg.initOwner(owner);
		dlg.setTitle("Delete Network");
		dlg.setHeaderText("Delete the network?");
		dlg.setContentText("If you go back, your peer will be shut down and your network deleted. Continue?");
		dlg.showAndWait();

		yes = dlg.getResult() == ButtonType.OK;

		return yes;
	}

	@FXML
	public void createNetworkAction(ActionEvent event) {
		clearError();
		if (!nodeManager.isConnected()) {
			if (nodeManager.createNetwork()) {
				btnCreate.setText("Continue");
				logger.debug("Network created (Host address: {})", txtIPAddress.getText());
			} else {
				setError("Could not create network.");
				logger.error("Could not create network (createNode returned false).");
			}
		}
		fNavigationService.navigate(ViewNames.REGISTER_VIEW);
	}

	private void setError(String error) {
		lblError.setText(error);
	}

	private void clearError() {
		lblError.setText("");
	}
}

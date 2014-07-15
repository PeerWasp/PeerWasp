package org.peerbox.controller;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.UUID;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CreateNetworkController implements Initializable {

	public void initialize(URL arg0, ResourceBundle arg1) {
		if(node == null){
			System.out.println("Try to create network...");
			node = H2HNode.createNode(NetworkConfiguration.create(generateNodeID()),
					FileConfiguration.createCustom(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize));
			node.getUserManager().configureAutostart(false);
			node.getFileManager().configureAutostart(false);
			ipOutputAddress.setText(getInetAddressAsString());
		}
		
	}
	
	@FXML
	private Button btnBackToSelection;
	
	@FXML
	private TextField ipOutputAddress;
	
	private IH2HNode node;
	private BigInteger maxFileSize = H2HConstants.DEFAULT_MAX_FILE_SIZE;
	private int maxNumOfVersions = H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS;
	private BigInteger maxSizeAllVersions = H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS;
	private int chunkSize = H2HConstants.DEFAULT_CHUNK_SIZE;
	
	public void goBackToSelection(ActionEvent event){
		System.out.println("Go back.");	
		MainNavigator.goBack();
	}
	
	public void createNetwork(ActionEvent event){
		MainNavigator.navigate("../SelectRootPathView.fxml");
	}
	
	public IH2HNode getNode(){
		return node;
	}
	
	private String generateNodeID() {
		return UUID.randomUUID().toString();
	}
	
	private String getInetAddressAsString(){
		InetAddress address;
		try {
			if(node.getNetworkConfiguration().isInitialPeer()){
				address = InetAddress.getLocalHost();
			} else {
				address = node.getNetworkConfiguration().getBootstrapAddress();
			}
			return address.getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "";
	}

}

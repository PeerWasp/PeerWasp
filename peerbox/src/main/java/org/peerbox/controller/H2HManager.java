package org.peerbox.controller;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;


public enum H2HManager {
	
	INSTANCE;
	
	private IH2HNode node;
	
	private UserCredentials userCredentials;
	private File rootDirectory = new File("blablabla");
	
	private BigInteger maxFileSize = H2HConstants.DEFAULT_MAX_FILE_SIZE;
	private int maxNumOfVersions = H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS;
	private BigInteger maxSizeAllVersions = H2HConstants.DEFAULT_MAX_SIZE_OF_ALL_VERSIONS;
	private int chunkSize = H2HConstants.DEFAULT_CHUNK_SIZE;
	
	public IH2HNode getNode(){
		return node;
	}
	
	public String generateNodeID() {
		return UUID.randomUUID().toString();
	}
	
	public void createNode(){
		node = H2HNode.createNode(NetworkConfiguration.create(generateNodeID()),
				FileConfiguration.createCustom(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize));
		node.getUserManager().configureAutostart(false);
		node.getFileManager().configureAutostart(false);

	}
	
	public void createNode(INetworkConfiguration configuration){
		node = H2HNode.createNode(configuration, FileConfiguration.createCustom(maxFileSize, maxNumOfVersions, maxSizeAllVersions, chunkSize));
		node.getUserManager().configureAutostart(false);
		node.getFileManager().configureAutostart(false);
	}
	
	public String getInetAddressAsString(){
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
	
	public boolean checkIfRegistered(String userName) throws NoPeerConnectionException{
		return node.getUserManager().isRegistered(userName);
	}

	public void registerUser(String username, String password, String pin) 
			throws NoPeerConnectionException, InterruptedException {
		// TODO: assert that root path is set and exists!
		userCredentials = new UserCredentials(username, password, pin);
		if (!node.getUserManager().isRegistered(userCredentials.getUserId())) {
			node.getUserManager().register(userCredentials).await();
		}
	}
}

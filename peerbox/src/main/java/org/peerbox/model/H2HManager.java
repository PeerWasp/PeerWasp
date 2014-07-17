package org.peerbox.model;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.ProcessComponentListener;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.security.UserCredentials;


public enum H2HManager {
	
	INSTANCE;
	
	private IH2HNode node;
	
	private UserCredentials userCredentials;
	private Path rootDirectory;
	
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
		node.connect();

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

	public boolean registerUser(String username, String password, String pin) 
			throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// TODO: assert that root path is set and exists!
				
		IUserManager userManager = node.getUserManager();
		System.out.println(String.format("'%s', '%s', '%s'", username, password, pin));
		userCredentials = new UserCredentials(username, password, pin);

		IProcessComponent registerProcess = userManager.register(userCredentials);
		ProcessComponentListener listener = new ProcessComponentListener();
		registerProcess.attachListener(listener);
		registerProcess.start().await();

		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			System.out.println((String.format("The process has failed: %s", reason != null ? ": " + reason.getHint() : ".")));
		}
	
		return listener.hasSucceeded() && userManager.isRegistered(userCredentials.getUserId());
		
	}
	
	public boolean loginUser(String username, String password, String pin) 
			throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// TODO: what to do with the user credentials? where to get them?
		System.out.println(String.format("'%s', '%s', '%s'", username, password, pin));
		userCredentials = new UserCredentials(username, password, pin);
		
		IProcessComponent process = node.getUserManager().login(userCredentials, rootDirectory);
		
		
		
		ProcessComponentListener listener = new ProcessComponentListener();

		process.attachListener(listener);
		process.start().await();

		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			System.out.println(String.format("The process has failed%s", reason != null ? ": " + reason.getHint() : "."));
		}
		
		System.out.println(node.getUserManager().isLoggedIn(userCredentials.getUserId())); // TODO: why does this return FALSE after login?
		return listener.hasSucceeded();
	}
	
	
	/**
	 * Tries to access the PeerBox network using a specified node's address or hostname.
	 * Returns false if the provided String is empty or no node is found at the specified
	 * address, true if the connection was successful.
	 * @param bootstrapAddressString contains the host's name or address.
	 * @throws UnknownHostException if the provided host is rejected (bad format).
	 */
	public boolean accessNetwork(String bootstrapAddressString) throws UnknownHostException{
		if(bootstrapAddressString.isEmpty()){
			System.out.println("No host provided.");
			return false;
		}
		String nodeID = H2HManager.INSTANCE.generateNodeID();
		InetAddress bootstrapAddress = InetAddress.getByName(bootstrapAddressString);
		H2HManager.INSTANCE.createNode(NetworkConfiguration.create(nodeID, bootstrapAddress));
		if(H2HManager.INSTANCE.getNode().connect()){
			System.out.println("Joined the network.");
			return true;
		} else {
			System.out.println("Was not able to join network!");
			return false;
		}
	}

	/**
	 * Sets the root directory path to the provided parameter. If the directory does not exist,
	 * it is created on the fly.
	 * @param rootDirectoryPath contains the absolute path to the root directory.
	 * @throws IOException if the provided rootDirectoryPath leads to a real file.
	 * @return true if File.mkdir() was successful.
	 */
	public boolean initializeRootDirectory(String rootDirectoryPath) throws IOException {
		File rootDirectoryFile = new File(rootDirectoryPath);
		boolean isDirectoryCreated = true;
		if(rootDirectoryFile.exists()){
			if(!rootDirectoryFile.isDirectory()){
				throw new IOException("The provided path leads to a file, not a directory.");
			}
			
		} else {
			//check if parent directory exist and is writable
			File parentDirectory = rootDirectoryFile.getParentFile();
			if(parentDirectory == null || !parentDirectory.canWrite()){
				return false;
			}
			//create the directory, only set rootDirectory if successful
			isDirectoryCreated = rootDirectoryFile.mkdir();
			if(isDirectoryCreated){
				rootDirectory = Paths.get(rootDirectoryFile.getAbsolutePath());
			}
		}
		return isDirectoryCreated;
	}
}

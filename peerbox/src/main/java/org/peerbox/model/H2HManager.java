package org.peerbox.model;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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

import com.google.inject.Singleton;

@Singleton
public class H2HManager {

	private IH2HNode node;
	private UserCredentials userCredentials;
	private Path rootPath;

	public IH2HNode getNode() {
		return node;
	}
	
	public String generateNodeID() {
		return UUID.randomUUID().toString();
	}
	
	public boolean createNode() {
		INetworkConfiguration defaultNetworkConf = NetworkConfiguration.create(generateNodeID());
		return createNode(defaultNetworkConf);
	}
	
	public boolean createNode(INetworkConfiguration configuration){
		node = H2HNode.createNode(configuration, FileConfiguration.createDefault());
		node.getUserManager().configureAutostart(false);
		node.getFileManager().configureAutostart(false);
		return node.connect();
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
	
	public boolean userIsRegistered(String userName) throws NoPeerConnectionException{
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
		
		IProcessComponent process = node.getUserManager().login(userCredentials, rootPath);
		
		
		
		ProcessComponentListener listener = new ProcessComponentListener();

		process.attachListener(listener);
		process.start().await();

		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			System.out.println(String.format("The process has failed%s", reason != null ? ": " + reason.getHint() : "."));
		}
		
		return listener.hasSucceeded() && node.getUserManager().isLoggedIn(userCredentials.getUserId());
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
		String nodeID = generateNodeID();
		InetAddress bootstrapAddress = InetAddress.getByName(bootstrapAddressString);
		boolean success = createNode(NetworkConfiguration.create(nodeID, bootstrapAddress));
		if(success){
			System.out.println("Joined the network.");
		} else {
			System.out.println("Was not able to join network!");
		}
		return success;
	}

	/**
	 * Sets the root directory path to the provided parameter. If the directory does not exist,
	 * it is created on the fly.
	 * @param rootDirectoryPath contains the absolute path to the root directory.
	 * @throws IOException if the provided rootDirectoryPath leads to a real file.
	 * @return true if the selected directory is valid and can be used. 
	 * @return false if either the parent directory does not exist or the user does not have write permissions.
	 */
	public boolean initializeRootDirectory(String rootDirectoryPath) throws IOException {
		File rootDirectoryFile = new File(rootDirectoryPath);
		boolean initializedSuccessfull = true;
		if(rootDirectoryFile.exists()){
			if(!rootDirectoryFile.isDirectory()){
				throw new IOException("The provided path leads to a file, not a directory.");
			}
			if(!Files.isWritable(rootDirectoryFile.toPath())){
				initializedSuccessfull = false;
			}
			
		} else {
			//check if parent directory exist and is writable
			File parentDirectory = rootDirectoryFile.getParentFile();
			if(parentDirectory == null || !Files.isWritable(parentDirectory.toPath())){
				return false;
			}
			//create the directory, only set rootDirectory if successful
			initializedSuccessfull = rootDirectoryFile.mkdir();
		}
		
		if(initializedSuccessfull){
			rootPath = Paths.get(rootDirectoryFile.getAbsolutePath());
		}
		return initializedSuccessfull;
	}

	public Path getRootPath() {
		return rootPath;
	}

	public void disconnectNode() {
		if(node != null){
			node.disconnect();	
		}
	}

	public void setRootPath(String path) {
		rootPath = new File(path).toPath();
	}
}

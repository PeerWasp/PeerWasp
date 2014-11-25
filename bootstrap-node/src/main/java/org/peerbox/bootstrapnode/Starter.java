package org.peerbox.bootstrapnode;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.security.UserCredentials;

public class Starter {
	
	private static final String PIN = "123";
	private static final String PASSWORD = "123456";
	private static final String USER = "user";
	private static final String ROOT_DIRECTORY = "/tmp/peerbox";



	public static void main(String[] args) {
		try {
			
			INetworkConfiguration netConfig = null;
			
			if(args.length == 0) {
				// initial peer
				System.out.println("Initial peer");
				netConfig = NetworkConfiguration.createInitial();
			} else if(args.length >= 1) {
				// bootstrap to node
				System.out.println("Connect to other node");
				InetAddress address = InetAddress.getByName(args[0]);
				netConfig = NetworkConfiguration.create(UUID.randomUUID().toString(),  address);
			}
		
			IFileConfiguration fileConfig = FileConfiguration.createDefault();
			IH2HNode peerNode = H2HNode.createNode(netConfig, fileConfig);
			peerNode.connect();
			
			if(args.length == 2) {
				// register if not exists and login
				registerAndLogin(peerNode);
			}
		
			// do not exit
			Thread.currentThread().join();
		
		} catch (InterruptedException | NoPeerConnectionException | IOException e) {
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	private static void registerAndLogin(IH2HNode peerNode) throws NoPeerConnectionException, InterruptedException, IOException {
		IUserManager userManager = peerNode.getUserManager();

		UserCredentials credentials = new UserCredentials(USER, PASSWORD, PIN);
		Path rootDirectory = Paths.get(ROOT_DIRECTORY);
		if(!Files.exists(rootDirectory)) {
			Files.createDirectory(rootDirectory);
		}

		if (!userManager.isRegistered(credentials.getUserId())) {
		    userManager.register(credentials).await();
		}
		userManager.login(credentials, new FileAgent(rootDirectory)).await();
	}
	
	
	
	
	private static class FileAgent implements IFileAgent {
		private Path rootDirectory;
		
		public FileAgent(Path rootDirectory) {
			this.rootDirectory = rootDirectory;
		}

		@Override
		public File getRoot() {
			return rootDirectory.toFile();
		}

		@Override
		public void writeCache(String key, byte[] data) throws IOException {
			// TODO Auto-generated method stub
		}

		@Override
		public byte[] readCache(String key) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}

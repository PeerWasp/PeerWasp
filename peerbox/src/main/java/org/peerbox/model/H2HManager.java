package org.peerbox.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class H2HManager {

	private static final Logger logger = LoggerFactory.getLogger(H2HManager.class);

	private IH2HNode node;

	public IH2HNode getNode() {
		return node;
	}

	public String generateNodeID() {
		return UUID.randomUUID().toString();
	}

	public boolean createNode() {
		INetworkConfiguration defaultNetworkConf = NetworkConfiguration.createInitial(generateNodeID());
		return createNode(defaultNetworkConf);
	}

	public boolean createNode(INetworkConfiguration configuration) {
		node = H2HNode.createNode(configuration, FileConfiguration.createDefault());
		node.getUserManager().configureAutostart(false);
		node.getFileManager().configureAutostart(false);
		return node.connect();
	}

	public String getInetAddressAsString() {
		InetAddress address;
		try {
			if (node.getNetworkConfiguration().isInitialPeer()) {
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

	/**
	 * Tries to access the PeerBox network using a specified node's address or hostname.
	 * Returns false if the provided String is empty or no node is found at the specified
	 * address, true if the connection was successful.
	 * 
	 * @param bootstrapAddressString contains the host's name or address.
	 * @throws UnknownHostException if the provided host is rejected (bad format).
	 */
	public boolean joinNetwork(String bootstrapAddressString) throws UnknownHostException {
		if (bootstrapAddressString.isEmpty()) {
			throw new IllegalArgumentException("Bootstrap address is empty.");
		}
		String nodeID = generateNodeID();
		InetAddress bootstrapAddress = InetAddress.getByName(bootstrapAddressString);
		return createNode(NetworkConfiguration.create(nodeID, bootstrapAddress));
	}

	public boolean joinNetwork(List<String> bootstrappingNodes)  {
		// TODO: maybe introduce try/catch block within the loop and do not throw exception.
		// required to finish the loop in all cases?
		boolean connected = false;
		Iterator<String> nodeIt = bootstrappingNodes.iterator();
		while (nodeIt.hasNext() && !connected) {
			String node = nodeIt.next();
			boolean success = false;
			try {
				success = joinNetwork(node);
			} catch(UnknownHostException e) {
				success = false;
			}
			
			connected = isConnected();
			if (success && connected) {
				logger.debug("Successfully connected to node {}", node);
			} else {
				logger.debug("Could not connect to node {}", node);
			}
		}
		return connected;
	}

	public boolean leaveNetwork() {
		if (node != null) {
			return node.disconnect();
		}
		return false;
	}

	public boolean isConnected() {
		return node != null && node.isConnected();
	}
}

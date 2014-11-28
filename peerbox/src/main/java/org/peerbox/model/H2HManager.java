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
public final class H2HManager {

	private static final Logger logger = LoggerFactory.getLogger(H2HManager.class);

	private IH2HNode node;

	public IH2HNode getNode() {
		return node;
	}

	private String generateNodeID() {
		return UUID.randomUUID().toString();
	}

	private void createNode() {
		INetworkConfiguration defaultNetConf = NetworkConfiguration.createInitial(generateNodeID());
		createNode(defaultNetConf);
	}
	
	private void createNode(String bootstrapAddress) throws UnknownHostException {
		String nodeID = generateNodeID();
		InetAddress bootstrapInetAddress = InetAddress.getByName(bootstrapAddress);
		createNode(NetworkConfiguration.create(nodeID, bootstrapInetAddress));
	}

	private void createNode(INetworkConfiguration netConfig) {
		node = H2HNode.createNode(netConfig, FileConfiguration.createDefault());
		node.getUserManager().configureAutostart(false);
		node.getFileManager().configureAutostart(false);
		
	}

	public boolean joinNetwork() {
		createNode();
		return node.connect();
	}
	
	public boolean joinNetwork(List<String> bootstrappingNodes) {
		boolean connected = false;
		Iterator<String> nodeIt = bootstrappingNodes.iterator();
		while (nodeIt.hasNext() && !connected) {
			String node = nodeIt.next();
			boolean res = false;
			try {
				res = joinNetwork(node);
				connected = isConnected();
				if (res && connected) {
					logger.debug("Successfully connected to node {}", node);
				} else {
					logger.debug("Could not connect to node {}", node);
				}
			} catch(UnknownHostException e) {
				logger.warn("Address of host could not be determined: {}", node);
				res = false;
				connected = false;
			}
		}
		return connected;
	}
	
	public boolean joinNetwork(String address) throws UnknownHostException {
		if (address.isEmpty()) {
			throw new IllegalArgumentException("Bootstrap address is empty.");
		}
		
		createNode(address);
		return node.connect();
	}

	public boolean leaveNetwork() {
		if (node != null) {
			boolean res = node.disconnect();
			node = null;
			return res;
		}
		
		return true;
	}
	
	public boolean isConnected() {
		return node != null && node.isConnected();
	}
}

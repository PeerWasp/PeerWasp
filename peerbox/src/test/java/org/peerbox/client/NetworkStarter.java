package org.peerbox.client;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.RandomUtils;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkStarter extends AbstractStarter {

	private static final Logger logger = LoggerFactory.getLogger(NetworkStarter.class);

	// number of nodes in the network
	private static final int NETWORK_SIZE = 15;

	// number of clients to login (with same user / credentials)
	private static final int NUM_CLIENTS = 2;

	private List<IH2HNode> network;
	private List<ClientNode> clients;

	public static void main(String[] args) {
		try {
			new NetworkStarter().start();
		} catch (IOException e) {
			logger.warn("staring network failed: {}", e.getMessage(), e);
		}
	}

	public NetworkStarter() throws IOException {
		super();
	}

	public void start() {
		try {

			setup();

		} catch (Exception e) {
			logger.warn("staring network failed: {}", e.getMessage(), e);
		}
	}

	private void setup() throws Exception {
		clients = Collections.synchronizedList(new ArrayList<>());
		network = NetworkTestUtil.createH2HNetwork(NETWORK_SIZE);
		logger.info("Create network with {} nodes.", NETWORK_SIZE);

		// register specific user
		credentials = new UserCredentials("user", "password", "pin");
		IH2HNode registerNode = network.get(0);
		registerUser(registerNode, credentials);

		// create and login all clients (same credentials)
		Set<Integer> usedNodes = new HashSet<>();
		int clientIndex = 0;
		while (clientIndex < NUM_CLIENTS) {
			int nodeIndex = RandomUtils.nextInt(0, network.size());
			if (!usedNodes.contains(nodeIndex)) {
				usedNodes.add(nodeIndex);
				Path path = BASE_PATH.resolve(String.format("client-%s", clientIndex));
				clients.add(new ClientNode(network.get(nodeIndex), credentials, path));
				logger.info("Created client {}/{} with node {}", clientIndex+1, NUM_CLIENTS, nodeIndex);
				++clientIndex;
			}
		}
	}

	public void stop() {
		teardown();
	}

	private void teardown() {
		logger.info("Stopping clients.");
		for(ClientNode node : clients) {
			node.stop();
		}
		logger.info("Shutdown network");
		NetworkTestUtil.shutdownH2HNetwork(network);
	}

	public List<ClientNode> getClients() {
		return clients;
	}

	public int getClientSize() {
		return clients.size();
	}

	public ClientNode getClientNode(int index) {
		return clients.get(index);
	}

	public List<Path> getRootPaths() {
		List<Path> paths = new ArrayList<>();
		for (ClientNode c : clients) {
			paths.add(c.getRootPath());
		}
		return paths;
	}
}

package com.peerwasp.bootstrapnode;

import java.net.InetAddress;
import java.util.UUID;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts a single node. If the node should be created as an initial peer, do
 * not give any command-line arguments. If the node should connect to an already
 * existing peer, give the address as command-line argument.
 *
 * @author albrecht
 *
 */
public class NodeStarter {

	private static final Logger logger = LoggerFactory.getLogger(NodeStarter.class);

	public static void main(String[] args) {
		try {

			INetworkConfiguration netConfig = null;

			if (args.length == 0) {

				// initial peer
				netConfig = NetworkConfiguration.createInitial();
				logger.info("Start as initial peer");

			} else if (args.length >= 1) {

				// connect to node
				String nodeId = UUID.randomUUID().toString();
				InetAddress address = InetAddress.getByName(args[0]);
				netConfig = NetworkConfiguration.create(nodeId, address);
				logger.info("Connect to existing peer. Address: {}", address.toString());

			}

			IFileConfiguration fileConfig = FileConfiguration.createDefault();
			IH2HNode peerNode = H2HNode.createNode(fileConfig);
			boolean success = peerNode.connect(netConfig);

			if (success) {
				logger.info("Setup successful.");
			} else {
				logger.error("Could not setup node.");
				System.exit(-1);
			}

			// do not exit
			logger.info("Finished creating node.");
			Thread.currentThread().join();

		} catch (Exception e) {
			logger.warn("Exception: {}", e.getMessage(), e);
		}

	}
}

package org.peerbox.bootstrapnode;

import java.io.IOException;
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
 * Creates a network of multiple nodes.
 * First, an initial peer is created. Second, all other peers connect to the (local) initial peer.
 *
 * @author albrecht
 *
 */
public class ManyNodesStarter {

	private static final Logger logger = LoggerFactory
			.getLogger(ManyNodesStarter.class);

	private static int NUM_NODES = 15;

	public static void main(String[] args) {

		if(args.length == 1) {
			NUM_NODES = Integer.valueOf(args[0]);
		}
		logger.info("Starting {} nodes.", NUM_NODES);

		for (int i = 0; i < NUM_NODES; ++i) {

			try {

				INetworkConfiguration netConfig = null;
				if (i == 0) {
					logger.info("Create initial peer");
					netConfig = NetworkConfiguration.createInitial();
				} else {
					logger.info("Create new peer and connect to initial peer");
					InetAddress address = InetAddress.getLocalHost();
					netConfig = NetworkConfiguration.create(UUID.randomUUID().toString(), address);
				}

				IFileConfiguration fileConfig = FileConfiguration.createDefault();
				IH2HNode peerNode = H2HNode.createNode(fileConfig);
				boolean success = peerNode.connect(netConfig);
				if (!success) {
					logger.error("Could not connect to initial peer");
				}

				logger.info("Node {}/{} up and running.", i+1, NUM_NODES);

			} catch (IOException e) {
				logger.warn("Exception: {}", e.getMessage(), e);
			}

		}

		// do not exit
		try {
			logger.info("Finished creating {} peers.", NUM_NODES);
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			logger.warn("Exception: {}", e.getMessage(), e);
		}

	}
}

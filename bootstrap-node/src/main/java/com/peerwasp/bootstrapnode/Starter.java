package com.peerwasp.bootstrapnode;

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
 * Starts a single node.
 * If the node should be created as an initial peer, do not give any command-line arguments.
 * If the node should connect to an already existing peer, give the address as command-line argument.
 *
 * @author albrecht
 *
 */
public class Starter {

	private static final Logger logger = LoggerFactory.getLogger(Starter.class);

	public static void main(String[] args) {
		try {

			INetworkConfiguration netConfig = null;

			if(args.length == 0) {
				// initial peer
				logger.info("Start as initial peer");
				netConfig = NetworkConfiguration.createInitial();
			} else if(args.length >= 1) {
				// bootstrap to node
				logger.info("Connect to existing peer");
				InetAddress address = InetAddress.getByName(args[0]);
				netConfig = NetworkConfiguration.create(UUID.randomUUID().toString(),  address);
			}

			IFileConfiguration fileConfig = FileConfiguration.createDefault();
			IH2HNode peerNode = H2HNode.createNode(fileConfig);
			boolean success = peerNode.connect(netConfig);
			if(!success) {
				logger.error("Could not connect to other node: {}", args[0]);
			}

			// do not exit
			Thread.currentThread().join();

		} catch (InterruptedException | IOException e) {
			logger.warn("Exception: {}", e.getMessage(), e);
		}

	}
}

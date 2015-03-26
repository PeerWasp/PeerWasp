package com.peerwasp.node;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.tomp2p.p2p.Peer;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a network of multiple nodes. First, an initial peer is created.
 * Second, all other peers connect to the (local) initial peer.
 *
 * @author albrecht
 *
 */
public class ManyNodesStarter {

	private static final Logger logger = LoggerFactory.getLogger(ManyNodesStarter.class);

	private static int NUM_NODES = 5;

	private static List<IH2HNode> nodes;

	private static IH2HNode initialNode;

	public static void main(String[] args) {

		try {

			// get number of nodes from commandline argument
			if (args.length == 1) {
				NUM_NODES = Integer.valueOf(args[0]);
			}
			logger.info("Starting {} nodes.", NUM_NODES);

			// create all nodes
			nodes = new ArrayList<>();
			for (int i = 0; i < NUM_NODES; ++i) {

				try {

					INetworkConfiguration netConfig = null;
					if (i == 0) {

						// first peer (initial)
						logger.info("Create initial peer");
						netConfig = NetworkConfiguration.createInitial();

					} else {

						logger.info("Create peer {} and connect to initial peer", i + 1);
						String nodeId = UUID.randomUUID().toString();
						Peer initialPeer = initialNode.getPeer().peer();
						netConfig = NetworkConfiguration.createLocalPeer(nodeId, initialPeer);

					}

					IFileConfiguration fileConfig = FileConfiguration.createDefault();
					IH2HNode peerNode = H2HNode.createNode(fileConfig);
					boolean success = peerNode.connect(netConfig);

					if (success) {
						nodes.add(peerNode);
						logger.info("Node {}/{} up and running.", i + 1, NUM_NODES);

						if (i == 0) {
							initialNode = peerNode;
						}
					} else {
						logger.error("Could not connect node {} to initial peer", i + 1);
					}



				} catch (Exception e) {
					logger.warn("Exception: {}", e.getMessage(), e);
				}

			}


			// do not exit
			logger.info("Finished creating {} nodes.", NUM_NODES);
			Thread.currentThread().join();

		} catch (Exception e) {
			logger.warn("Exception: {}", e.getMessage(), e);
		}

	}
}

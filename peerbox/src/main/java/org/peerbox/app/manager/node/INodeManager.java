package org.peerbox.app.manager.node;

import java.net.UnknownHostException;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;


/**
 * The node manager is responsible for creating and configuring the node respectively
 * network related issues.
 *
 * @author albrecht
 *
 */
public interface INodeManager {

	/**
	 * Get the node of H2H. Must create or join network first.
	 *
	 * @return h2h node (may return null)
	 */
	IH2HNode getNode();

	/**
	 * Join a network. Iterates through the given list of node addresses and tries to connect to
	 * the node.
	 *
	 * @param bootstrappingNodes list of node addresses
	 * @return true if join succeeds. False otherwise.
	 */
	boolean joinNetwork(List<String> bootstrappingNodes);

	/**
	 * Join a network by connecting to the given address.
	 *
	 * @param address
	 * @return true if join succeeds. False otherwise.
	 * @throws UnknownHostException If address cannot be resolved.
	 */
	boolean joinNetwork(String address) throws UnknownHostException;

	/**
	 * Creates a new network. This results in a local initial peer.
	 *
	 * @return true if operation succeeds. False otherwise.
	 */
	boolean createNetwork();

	/**
	 * Disconnect from network (graceful leave).
	 *
	 * @return true if operation succeeds. False otherwise.
	 */
	boolean leaveNetwork();

	/**
	 * Check whether node is connected. See {@link IH2HNode#isConnected()}.
	 *
	 * @return
	 */
	boolean isConnected();

	/**
	 * Get the network configuration of the node. Note: must first create or join network.
	 *
	 * @return network configuration (may return null).
	 */
	INetworkConfiguration getNetworkConfiguration();

	/**
	 * Get the file configuration of the node. Note: must first create or join network.
	 *
	 * @return file configuration (may return null).
	 */
	IFileConfiguration getFileConfiguration();

}

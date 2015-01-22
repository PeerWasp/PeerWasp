package org.peerbox.testutils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.node.NodeManager;

public class NetworkTestUtil {
	public static List<INodeManager> createNetwork(int size) {
		if(size < 1) {
			throw new IllegalArgumentException("size must be >= 1");
		}

		List<INodeManager> nodes = new ArrayList<>();
		INodeManager initial = new NodeManager(null);
		initial.createNetwork();
		nodes.add(initial);

		for (int i = 1; i < size; ++i) {
			try {
				INodeManager n = new NodeManager(null);
				n.joinNetwork("localhost");
				nodes.add(n);
			} catch (UnknownHostException e) {
				// should not happen... on local host
				e.printStackTrace();
			}
		}

		return nodes;
	}

	public static void shutdownNetwork(List<INodeManager> nodes) {
		for (INodeManager node : nodes) {
			node.leaveNetwork();
		}
	}
}

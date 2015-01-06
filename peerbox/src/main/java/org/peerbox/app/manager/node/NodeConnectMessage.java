package org.peerbox.app.manager.node;

public class NodeConnectMessage implements INodeMessage {

	private final String nodeAddress;

	public NodeConnectMessage(final String nodeAddress) {
		this.nodeAddress = nodeAddress;
	}

	public String getNodeAddress() {
		return nodeAddress;
	}
	
}

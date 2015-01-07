package org.peerbox.app.manager.node;

import net.engio.mbassy.listener.Handler;

public interface INodeMessageListener {

	@Handler
	void onNodeConnected(NodeConnectMessage message);

	@Handler
	void onNodeDisconnected(NodeDisconnectMessage message);

}

package org.peerbox.app.manager.node;

import org.peerbox.events.IMessageListener;

import net.engio.mbassy.listener.Handler;

public interface INodeMessageListener extends IMessageListener {

	@Handler
	void onNodeConnected(NodeConnectMessage message);

	@Handler
	void onNodeDisconnected(NodeDisconnectMessage message);

}

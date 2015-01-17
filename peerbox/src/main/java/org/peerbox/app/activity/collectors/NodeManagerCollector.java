package org.peerbox.app.activity.collectors;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.manager.node.INodeMessageListener;
import org.peerbox.app.manager.node.NodeConnectMessage;
import org.peerbox.app.manager.node.NodeDisconnectMessage;

import com.google.inject.Inject;

/**
 * Node Messages (connected, disconnected, ...)
 *
 * @author albrecht
 *
 */
final class NodeManagerCollector extends AbstractActivityCollector implements INodeMessageListener {

	@Inject
	protected NodeManagerCollector(ActivityLogger activityLogger) {
		super(activityLogger);
	}

	@Handler
	@Override
	public void onNodeConnected(NodeConnectMessage message) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Node connected.")
				.setDescription(String.format(
						"Client connected to the network. Address connected to: %s",
						message.getNodeAddress()));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onNodeDisconnected(NodeDisconnectMessage message) {
		ActivityItem item = ActivityItem.create()
				.setTitle("Node disconnected.")
				.setDescription("Node disconnected from thet network.");
		getActivityLogger().addActivityItem(item);
	}

}

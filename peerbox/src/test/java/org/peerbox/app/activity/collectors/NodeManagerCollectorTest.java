package org.peerbox.app.activity.collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.app.manager.node.NodeConnectMessage;
import org.peerbox.app.manager.node.NodeDisconnectMessage;

public class NodeManagerCollectorTest extends BaseJUnitTest {

	private NodeManagerCollector collector;
	private ActivityLogger activityLogger;

	@Before
	public void setUp() throws Exception {
		activityLogger = Mockito.mock(ActivityLogger.class);
		collector = new NodeManagerCollector(activityLogger);
	}

	@After
	public void tearDown() throws Exception {
		activityLogger = null;
		collector = null;
	}

	@Test
	public void testNodeManagerCollector() {
		assertEquals(collector.getActivityLogger(), activityLogger);
	}

	@Test
	public void testOnNodeConnected() {
		NodeConnectMessage msg = new NodeConnectMessage("127.0.0.1");
		collector.onNodeConnected(msg);

		captureAddActivityItem(ActivityType.INFORMATION);
	}

	@Test
	public void testOnNodeDisconnected() {
		NodeDisconnectMessage msg = new NodeDisconnectMessage();
		collector.onNodeDisconnected(msg);

		captureAddActivityItem(ActivityType.INFORMATION);
	}

	/**
	 * captures and verifies addActivityItem argument
	 */
	private void captureAddActivityItem(ActivityType expectedType) {
		ArgumentCaptor<ActivityItem> arg = ArgumentCaptor.forClass(ActivityItem.class);
		Mockito.verify(activityLogger, times(1)).addActivityItem(arg.capture());
		arg.getValue().getType().equals(expectedType);
	}

}

package org.peerbox.app.activity.collectors;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
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

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnNodeDisconnected() {
		NodeDisconnectMessage msg = new NodeDisconnectMessage();
		collector.onNodeDisconnected(msg);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

}

package org.peerbox.app.activity.collectors;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.events.InformationMessage;
import org.peerbox.events.WarningMessage;

public class GeneralMessageCollectorTest extends BaseJUnitTest {

	private GeneralMessageCollector collector;
	private ActivityLogger activityLogger;

	@Before
	public void setUp() throws Exception {
		activityLogger = Mockito.mock(ActivityLogger.class);
		collector = new GeneralMessageCollector(activityLogger);
	}

	@After
	public void tearDown() throws Exception {
		activityLogger = null;
		collector = null;
	}

	@Test
	public void testGeneralMessageCollector() {
		assertEquals(collector.getActivityLogger(), activityLogger);
	}

	@Test
	public void testOnInformationMessage() {
		InformationMessage msg = new InformationMessage("title", "description");
		collector.onInformationMessage(msg);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnWarningMessage() {
		WarningMessage msg = new WarningMessage("title", "description");
		collector.onWarningMessage(msg);

		CollectorTestUtils.captureAddActivityItem(ActivityType.WARNING, activityLogger);
	}

}

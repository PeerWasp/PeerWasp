package org.peerbox.app.activity.collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.events.InformationMessage;
import org.peerbox.events.WarningMessage;

public class GeneralMessageCollectorTest {

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

		captureAddActivityItem(ActivityType.INFORMATION);
	}

	@Test
	public void testOnWarningMessage() {
		WarningMessage msg = new WarningMessage("title", "description");
		collector.onWarningMessage(msg);

		captureAddActivityItem(ActivityType.WARNING);
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

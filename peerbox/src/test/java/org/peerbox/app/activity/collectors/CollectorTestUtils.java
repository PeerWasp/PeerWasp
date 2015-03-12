package org.peerbox.app.activity.collectors;

import static org.mockito.Mockito.times;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;

class CollectorTestUtils extends BaseJUnitTest {

	/**
	 * captures and verifies addActivityItem argument
	 */
	public static void captureAddActivityItem(ActivityType expectedType, ActivityLogger activityLogger) {
		ArgumentCaptor<ActivityItem> arg = ArgumentCaptor.forClass(ActivityItem.class);
		Mockito.verify(activityLogger, times(1)).addActivityItem(arg.capture());
		arg.getValue().getType().equals(expectedType);
	}
}

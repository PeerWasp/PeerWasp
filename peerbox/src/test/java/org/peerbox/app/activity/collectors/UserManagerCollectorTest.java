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
import org.peerbox.app.manager.user.LoginMessage;
import org.peerbox.app.manager.user.LogoutMessage;
import org.peerbox.app.manager.user.RegisterMessage;

public class UserManagerCollectorTest extends BaseJUnitTest {

	private UserManagerCollector collector;
	private ActivityLogger activityLogger;

	@Before
	public void setUp() throws Exception {
		activityLogger = Mockito.mock(ActivityLogger.class);
		collector = new UserManagerCollector(activityLogger);
	}

	@After
	public void tearDown() throws Exception {
		activityLogger = null;
		collector = null;
	}

	@Test
	public void testUserManagerCollector() {
		assertEquals(collector.getActivityLogger(), activityLogger);
	}

	@Test
	public void testOnUserRegister() {
		RegisterMessage msg = new RegisterMessage("username");
		collector.onUserRegister(msg);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnUserLogin() {
		LoginMessage msg = new LoginMessage("username");
		collector.onUserLogin(msg);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnUserLogout() {
		LogoutMessage msg = new LogoutMessage("username");
		collector.onUserLogout(msg);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}


}

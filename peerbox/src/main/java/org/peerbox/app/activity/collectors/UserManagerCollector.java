package org.peerbox.app.activity.collectors;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.manager.user.IUserMessageListener;
import org.peerbox.app.manager.user.LoginMessage;
import org.peerbox.app.manager.user.LogoutMessage;
import org.peerbox.app.manager.user.RegisterMessage;

import com.google.inject.Inject;

/**
 * User Messages (login, logout, ...)
 *
 * @author albrecht
 *
 */
final class UserManagerCollector extends AbstractActivityCollector implements IUserMessageListener {

	@Inject
	protected UserManagerCollector(ActivityLogger activityLogger) {
		super(activityLogger);
	}

	@Handler
	@Override
	public void onUserRegister(RegisterMessage register) {
		ActivityItem item = ActivityItem.create()
				.setTitle("User registered.")
				.setDescription(String.format("Username: %s", register.getUsername()));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onUserLogin(LoginMessage login) {
		ActivityItem item = ActivityItem.create()
			.setTitle("User logged in.")
			.setDescription(String.format("Username: %s", login.getUsername()));
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onUserLogout(LogoutMessage logout) {
		ActivityItem item = ActivityItem.create()
				.setTitle("User logged out.")
				.setDescription(String.format("Username: %s", logout.getUsername()));
			getActivityLogger().addActivityItem(item);
	}

}

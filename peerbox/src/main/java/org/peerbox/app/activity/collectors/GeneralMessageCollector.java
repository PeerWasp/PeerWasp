package org.peerbox.app.activity.collectors;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.activity.ActivityItem;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.events.IGeneralMessageListener;
import org.peerbox.events.InformationMessage;
import org.peerbox.events.WarningMessage;

import com.google.inject.Inject;

final class GeneralMessageCollector extends AbstractActivityCollector implements IGeneralMessageListener {

	@Inject
	protected GeneralMessageCollector(ActivityLogger activityLogger) {
		super(activityLogger);
	}

	@Handler
	@Override
	public void onInformationMessage(InformationMessage message) {
		ActivityItem item = ActivityItem.create()
				.setTitle(message.getTitle())
				.setDescription(message.getDescription());
		getActivityLogger().addActivityItem(item);
	}

	@Handler
	@Override
	public void onWarningMessage(WarningMessage message) {
		ActivityItem item = ActivityItem.create()
				.setTitle(message.getTitle())
				.setDescription(message.getDescription())
				.setType(ActivityType.WARNING);
		getActivityLogger().addActivityItem(item);
	}

}

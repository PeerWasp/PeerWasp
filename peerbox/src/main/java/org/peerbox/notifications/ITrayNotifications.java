package org.peerbox.notifications;

import com.google.common.eventbus.Subscribe;

public interface ITrayNotifications {
	
	@Subscribe
	public void showInformation(InformationNotification in);
	
	@Subscribe
	public void showFileEvents(AggregatedFileEventStatus event);
	
}

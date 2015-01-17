package org.peerbox.app.activity.collectors;

import org.peerbox.app.activity.ActivityLogger;

/**
 * Manages common state and functionality between different activity collectors.
 * Concrete ActivityCollectors are responsible for converting messages into information for
 * the user by assembling ActivityItems and passing these items to the ActivityLogger.
 *
 * @author albrecht
 *
 */
abstract class AbstractActivityCollector implements IActivityCollector {

	private ActivityLogger activityLogger;

	protected AbstractActivityCollector(ActivityLogger activityLogger) {
		this.activityLogger = activityLogger;
	}

	protected ActivityLogger getActivityLogger() {
		return activityLogger;
	}

}

package org.peerbox.app.activity;

/**
 * An ActivityItem represents an event that occurred for which the user should possibly receive
 * a notification.
 * Offers a simple fluent API to set all properties.
 *
 * @author albrecht
 *
 */
public final class ActivityItem {

	private String title;
	private String description;
	private long timestamp;
	private ActivityType type;

	/**
	 * use static create() method to get new instance.
	 */
	private ActivityItem() {
		title = "";
		description = "";
		type = ActivityType.INFORMATION;
		timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new ActivityItem to use and configure.
	 *
	 * @return new activity item
	 */
	public static ActivityItem create() {
		return new ActivityItem();
	}

	public String getTitle() {
		return title;
	}

	public ActivityItem setTitle(final String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public ActivityItem setDescription(final String description) {
		this.description = description;
		return this;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public ActivityType getType() {
		return type;
	}

	public ActivityItem setType(final ActivityType type) {
		this.type = type;
		return this;
	}

}

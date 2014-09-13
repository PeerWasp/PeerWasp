package org.peerbox.notifications;

public class InformationNotification {

	private String title;
	private String message;

	public InformationNotification(String title, String message) {
		this.title = title;
		this.message = message;
	}
	
	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

}

package org.peerbox.notifications;

import org.peerbox.events.IMessage;


public class InformationNotification implements IMessage {

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

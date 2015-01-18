package org.peerbox.events;

public class InformationMessage extends AbstractGeneralMessage {

	public InformationMessage(String title, String description) {
		super(title, description);
	}

	public InformationMessage(String title) {
		super(title, null);
	}

}

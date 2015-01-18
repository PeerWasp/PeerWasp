package org.peerbox.events;

public class WarningMessage extends AbstractGeneralMessage {

	public WarningMessage(String title, String description) {
		super(title, description);
	}

	public WarningMessage(String title) {
		super(title, null);
	}

}

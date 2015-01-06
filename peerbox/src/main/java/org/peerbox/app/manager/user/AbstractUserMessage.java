package org.peerbox.app.manager.user;

abstract class AbstractUserMessage implements IUserMessage {

	private final String username;

	public AbstractUserMessage(final String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}

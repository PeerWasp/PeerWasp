package org.peerbox.server.servlets.messages;

public enum ServerReturnCode {
	WRONG_CONTENT_TYPE("Wrong ContentType, only JSON supported."),
	DESERIALIZE_ERROR("Could not deserialize message.");
	
	private String message;
	ServerReturnCode(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
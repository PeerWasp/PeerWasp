package org.peerbox.server.servlets.messages;


public class ServerReturnMessage {
	
	private int returnCode;
	private String message;
	
	public ServerReturnMessage(ServerReturnCode returnCode) {
		this.returnCode = returnCode.ordinal();
		this.message = returnCode.getMessage();
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getCode() {
		return returnCode;
	}
}

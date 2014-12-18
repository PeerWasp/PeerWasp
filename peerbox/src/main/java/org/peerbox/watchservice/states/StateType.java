package org.peerbox.watchservice.states;

public enum StateType {
	ABSTRACT("Abstract"),
	INITIAL("Initial"),
	ESTABLISHED("Established"),
	CONFLICT("Conflict"),
	
	LOCAL_CREATE("LocalCreate"),
	LOCAL_DELETE("LocalDelete"),
	LOCAL_UPDATE("LocalUpdate"),
	LOCAL_MOVE("LocalMove"),
	LOCAL_HARD_DELETE("LocalHardDelete"),
	LOCAL_RECOVER("LocalRecover"),

	REMOTE_CREATE("RemoteCreate"),
	REMOTE_UPDATE("RemoteUpdate"),
	REMOTE_MOVE("RemoteMove");

	
	private final String type;
	private StateType(String type){
		this.type = type;
	}
	
	public String getString(){
		return type;
	}
}

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

	REMOTE_CREATE("RemoteCreate"),
	REMOTE_UPDATE("RemoteUpdate"),
	REMOTE_MOVE("RemoteMove");


	private final String name;

	private StateType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}

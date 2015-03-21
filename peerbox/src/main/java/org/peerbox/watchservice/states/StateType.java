package org.peerbox.watchservice.states;

/**
 * This enum is used to assign types to the various states. This was done to
 * have a way to distinguish the state without using "instanceof".
 * @author Claudio
 *
 */
public enum StateType {
	ABSTRACT("Abstract"),
	INITIAL("Initial"),
	ESTABLISHED("Established"),

	LOCAL_CREATE("LocalCreate"),
	LOCAL_UPDATE("LocalUpdate"),
	LOCAL_MOVE("LocalMove"),
	LOCAL_HARD_DELETE("LocalHardDelete"),

	REMOTE_CREATE("RemoteCreate"),
	REMOTE_UPDATE("RemoteUpdate");


	private final String name;

	private StateType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}

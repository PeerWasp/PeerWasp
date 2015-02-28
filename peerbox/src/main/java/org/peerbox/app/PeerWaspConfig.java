package org.peerbox.app;

public class PeerWaspConfig implements IPeerWaspConfig {

	/** Amount of time that an action has to be "stable" in order to be executed **/
	public static final long ACTION_WAIT_TIME_MS = 10000;
	public static final int ACTION_WAIT_TIME_SEC = (int)(ACTION_WAIT_TIME_MS / 1000);
	
	/** Maximal number of concurrent network transactions **/
	public static final int NUMBER_OF_EXECUTE_SLOTS = 3;
	
	/** Maximal number of attempts to re-execute failed transactions **/
	public static final int MAX_EXECUTION_ATTEMPTS = 3;
	
	@Override
	public int getAggregationIntervalInSeconds() {
		return ACTION_WAIT_TIME_SEC;
	}

	@Override
	public long getAggregationIntervalInMillis() {
		return ACTION_WAIT_TIME_MS;
	}

	@Override
	public int getNumberOfExecutionSlots() {
		return NUMBER_OF_EXECUTE_SLOTS;
	}

	@Override
	public int getMaximalExecutionAttempts() {
		return MAX_EXECUTION_ATTEMPTS;
	}
}

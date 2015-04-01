package org.peerbox.app.config;

public class PeerWaspConfig implements IPeerWaspConfig {

	/** Amount of time that an action has to be "stable" in order to be executed **/
	public static final long AGGREGATION_TIME_MS = 2000;
	public static final int AGGREGATION_TIME_SEC = (int)(AGGREGATION_TIME_MS / 1000);

	public static final long LONG_AGGREGATION_TIME_MS = 10000;
	public static final int LONG_AGGREGATION_TIME_SEC = (int)(LONG_AGGREGATION_TIME_MS / 1000);

	/** Maximal number of concurrent network transactions **/
	public static final int NUMBER_OF_EXECUTE_SLOTS = 3;

	/** Maximal number of attempts to re-execute failed transactions **/
	public static final int MAX_EXECUTION_ATTEMPTS = 3;

	@Override
	public int getAggregationIntervalInSeconds() {
		return AGGREGATION_TIME_SEC;
	}

	@Override
	public long getAggregationIntervalInMillis() {
		return AGGREGATION_TIME_MS;
	}

	@Override
	public int getNumberOfExecutionSlots() {
		return NUMBER_OF_EXECUTE_SLOTS;
	}

	@Override
	public int getMaximalExecutionAttempts() {
		return MAX_EXECUTION_ATTEMPTS;
	}

	@Override
	public long getLongAggregationIntervalInMillis() {
		return LONG_AGGREGATION_TIME_MS;
	}

	@Override
	public long getLongAggregationIntervalInSeconds() {
		return LONG_AGGREGATION_TIME_SEC;
	}
}

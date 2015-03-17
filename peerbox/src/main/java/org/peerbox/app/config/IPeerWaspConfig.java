package org.peerbox.app.config;

public interface IPeerWaspConfig {

	int getAggregationIntervalInSeconds();
	long getAggregationIntervalInMillis();

	int getNumberOfExecutionSlots();
	int getMaximalExecutionAttempts();
}

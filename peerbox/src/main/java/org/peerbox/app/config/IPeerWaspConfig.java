package org.peerbox.app.config;

public interface IPeerWaspConfig {

	public int getAggregationIntervalInSeconds();
	public long getAggregationIntervalInMillis();
	
	public int getNumberOfExecutionSlots();
	public int getMaximalExecutionAttempts();	
}

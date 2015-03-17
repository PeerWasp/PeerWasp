package org.peerbox.app.config;

public interface IPeerWaspConfig {

	public int getAggregationIntervalInSeconds();
	public long getAggregationIntervalInMillis();
	
	public long getLongAggregationIntervalInMillis();
	public long getLongAggregationIntervalInSeconds();
	
	public int getNumberOfExecutionSlots();
	public int getMaximalExecutionAttempts();	
}

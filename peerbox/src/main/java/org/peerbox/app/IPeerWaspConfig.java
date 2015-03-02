package org.peerbox.app;

public interface IPeerWaspConfig {

	public int getAggregationIntervalInSeconds();
	public long getAggregationIntervalInMillis();
	
	public int getNumberOfExecutionSlots();
	public int getMaximalExecutionAttempts();	
}

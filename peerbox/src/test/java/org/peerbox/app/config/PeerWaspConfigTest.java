package org.peerbox.app.config;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PeerWaspConfigTest {

	private IPeerWaspConfig config;

	@Before
	public void setUp() throws Exception {
		config = new PeerWaspConfig();
	}

	@After
	public void tearDown() throws Exception {
		config = null;
	}

	@Test
	public void testGetAggregationIntervalInMillis() {
		assertTrue(config.getAggregationIntervalInMillis() >= 1);
	}

	@Test
	public void testGetNumberOfExecutionSlots() {
		assertTrue(config.getNumberOfExecutionSlots() >= 1);
	}

	@Test
	public void testGetMaximalExecutionAttempts() {
		assertTrue(config.getMaximalExecutionAttempts() >= 0);
	}

}

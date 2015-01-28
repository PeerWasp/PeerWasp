package org.peerbox;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseJUnitTest {

	private final static Logger logger = LoggerFactory.getLogger(BaseJUnitTest.class);

	/* global time out rule */
	// @Rule
	// public Timeout globalTimeout = new Timeout(10 * 60 * 1000);

	@ClassRule
	public static TestRule classWatchman = new TestWatcher() {
		@Override
		protected void starting(Description desc) {
			logger.info("=========================== STARTING ===========================");
			logger.info("Test class: {}", desc.getDisplayName());
			logger.info("================================================================");

		}

		@Override
		protected void finished(Description desc) {
			logger.info("=========================== FINISHED ===========================");
			logger.info("Test class: {}", desc.getDisplayName());
			logger.info("================================================================");
		}
	};

	@Rule
	public TestRule methodWatchman = new TestWatcher() {
		@Override
		protected void starting(Description desc) {
			logger.info("--------------------------- STARTING TEST ---------------------------");
			logger.info("Test method: {}", desc.getDisplayName());
		}

		@Override
		protected void finished(Description desc) {
			logger.info("Test method: {}", desc.getDisplayName());
			logger.info("--------------------------- FINISHED TEST ---------------------------");
		}

		@Override
		protected void failed(Throwable e, Description desc) {
			logger.error("### FAILED ### - Test method: {}", desc.getDisplayName());
		}

		@Override
		protected void succeeded(Description desc) {
			logger.info("### SUCCEEDED ### - Test method: {}", desc.getDisplayName());
		}

		@Override
		protected void skipped(AssumptionViolatedException e, Description desc) {
			logger.info("### SKIPPED ### - Test method: {}", desc.getDisplayName());
		}
	};

}
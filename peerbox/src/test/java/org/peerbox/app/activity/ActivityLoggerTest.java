package org.peerbox.app.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.peerbox.helper.JavaFXThreadingRule;

public class ActivityLoggerTest {

	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

	private final int MAX_ITEMS = 100;
	private ActivityLogger logger;

	@Before
	public void setUp() throws Exception {
		logger = new ActivityLogger();
	}

	@After
	public void tearDown() throws Exception {
		logger = null;
	}

	@Test
	public void testActivityLogger() {
		assertNotNull(logger);
		assertNotNull(logger.getActivityItems());
		assertTrue(logger.getActivityItems().isEmpty());
	}

	@Test
	public void testAddActivityItem() {
		ActivityItem item = ActivityItem.create();
		logger.addActivityItem(item);

		assertTrue(logger.getActivityItems().contains(item));
		assertEquals(logger.getActivityItems().size(), 1);
	}

	@Test
	public void testAddToLimit() {
		for (int i = 0; i < 2*MAX_ITEMS; ++i) {
			ActivityItem item = ActivityItem.create();
			logger.addActivityItem(item);
			assertTrue(logger.getActivityItems().contains(item));
			assertTrue(logger.getActivityItems().size() <= MAX_ITEMS);
		}
	}

	@Test
	public void testGetActivityItems() {
		assertNotNull(logger.getActivityItems());
	}

	@Test
	public void testClearActivityItems() {
		final int itemsToAdd = 20;
		for (int i = 0; i < itemsToAdd; ++i) {
			ActivityItem item = ActivityItem.create();
			logger.addActivityItem(item);
		}
		assertEquals(logger.getActivityItems().size(), itemsToAdd);

		logger.clearActivityItems();
		assertTrue(logger.getActivityItems().isEmpty());
	}

}

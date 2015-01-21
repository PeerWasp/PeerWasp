package org.peerbox.app.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.peerbox.BaseJUnitTest;

public class ActivityItemTest extends BaseJUnitTest {

	private ActivityItem item;
	private ActivityItem returnItem;

	@Before
	public void setUp() throws Exception {
		item = ActivityItem.create();
		returnItem = null;
	}

	@After
	public void tearDown() throws Exception {
		item = null;
		returnItem = null;
	}

	@Test
	public void testCreate() {
		ActivityItem i = ActivityItem.create();
		// initial state check
		assertNotNull(i);
		assertEquals("", i.getTitle());
		assertEquals("", i.getDescription());
		assertEquals(ActivityType.INFORMATION, i.getType());
		assertEquals(System.currentTimeMillis(), i.getTimestamp());
	}

	@Test
	public void testGetAndSetTitle() {
		assertEquals("", item.getTitle());

		returnItem = item.setTitle("this is a title");
		assertReturnThisItem();
		assertEquals("this is a title", item.getTitle());

		returnItem = item.setTitle(null);
		assertReturnThisItem();
		assertNull(item.getTitle());

		returnItem = item.setTitle("");
		assertReturnThisItem();
		assertEquals("", item.getTitle());
	}

	@Test
	public void testGeAndSetDescription() {
		assertEquals("", item.getTitle());

		returnItem = item.setDescription("this is a description");
		assertReturnThisItem();
		assertEquals("this is a description", item.getDescription());

		returnItem = item.setDescription(null);
		assertReturnThisItem();
		assertNull(item.getDescription());

		returnItem = item.setDescription("");
		assertReturnThisItem();
		assertEquals("", item.getDescription());
	}

	@Test
	public void testGetTimestamp() {
		assertTrue(item.getTimestamp() > 0);
		assertTrue((item.getTimestamp() - System.currentTimeMillis()) < 10);
	}

	@Test
	public void testGetAndSetType() {
		assertEquals(item.getType(), ActivityType.INFORMATION);

		returnItem = item.setType(ActivityType.WARNING);
		assertReturnThisItem();
		assertEquals(item.getType(), ActivityType.WARNING);

		returnItem = item.setType(ActivityType.INFORMATION);
		assertReturnThisItem();
		assertEquals(item.getType(), ActivityType.INFORMATION);
	}

	/**
	 * Asserts that returnItem is equals to the item.
	 * Useful for the fluent API setters that return "this" instance.
	 */
	private void assertReturnThisItem() {
		assertEquals(returnItem, item);
	}

}

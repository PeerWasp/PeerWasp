package org.peerbox.app.activity.collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActivityConfigurationTest {

	private ActivityConfiguration activity;

	@Before
	public void setUp() throws Exception {
		activity = new ActivityConfiguration();
	}

	@After
	public void tearDown() throws Exception {
		activity = null;
	}

	@Test
	public void testActivityConfiguration() {
		assertNotNull(activity);
		assertNull(activity.getGeneralMessageCollector());
		assertNull(activity.getNodeManagerCollector());
		assertNull(activity.getUserManagerCollector());
		assertNull(activity.getFileManagerCollector());
	}

	@Test
	public void testGetAndSetGeneralMessageCollector() {
		GeneralMessageCollector msg = new GeneralMessageCollector(null);
		activity.setGeneralMessageCollector(msg);
		assertEquals(activity.getGeneralMessageCollector(), msg);

		activity.setGeneralMessageCollector(null);
		assertNull(activity.getGeneralMessageCollector());
	}

	@Test
	public void testGetAndSetNodeManagerCollector() {
		NodeManagerCollector msg = new NodeManagerCollector(null);
		activity.setNodeManagerCollector(msg);
		assertEquals(activity.getNodeManagerCollector(), msg);

		activity.setNodeManagerCollector(null);
		assertNull(activity.getNodeManagerCollector());
	}

	@Test
	public void testGetAndSetUserManagerCollector() {
		UserManagerCollector msg = new UserManagerCollector(null);
		activity.setUserManagerCollector(msg);
		assertEquals(activity.getUserManagerCollector(), msg);

		activity.setUserManagerCollector(null);
		assertNull(activity.getUserManagerCollector());
	}

	@Test
	public void testGetFileManagerCollector() {
		FileManagerCollector msg = new FileManagerCollector(null);
		activity.setFileManagerCollector(msg);
		assertEquals(activity.getFileManagerCollector(), msg);

		activity.setFileManagerCollector(null);
		assertNull(activity.getFileManagerCollector());
	}

}

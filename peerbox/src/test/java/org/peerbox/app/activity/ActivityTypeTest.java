package org.peerbox.app.activity;

import static org.junit.Assert.*;

import org.junit.Test;
import org.peerbox.BaseJUnitTest;

public class ActivityTypeTest extends BaseJUnitTest {

	/**
	 * This test just checks the number of elements.
	 * If this test fails, it means that the number of elements of the enum changed which may
	 * have an effect on OTHER tests respectively other classes that use this enum.
	 */
	@Test
	public void testEnumElements() {
		for (ActivityType t : ActivityType.values()) {
			switch (t) {
				case INFORMATION:
					break;
				case WARNING:
					break;
				case ERROR:
					break;
				default:
					fail("ActivityType enum has more elements than expected. Missed: "
							+ t.toString());
			}
		}
	}

}

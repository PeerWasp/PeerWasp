package org.peerbox.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.peerbox.BaseJUnitTest;

public class NetUtilsTest extends BaseJUnitTest {

	@Test
	public void testIsValidPort() {
		assertFalse(NetUtils.isValidPort(-4714));
		assertFalse(NetUtils.isValidPort(-1));
		assertFalse(NetUtils.isValidPort(0));
		assertTrue(NetUtils.isValidPort(1));
		assertTrue(NetUtils.isValidPort(4714));
		assertTrue(NetUtils.isValidPort(65534));
		assertTrue(NetUtils.isValidPort(65535));
		assertFalse(NetUtils.isValidPort(65536));
	}

	@Test
	public void testMinMaxPort() {
		assertEquals(1, NetUtils.MIN_PORT);
		assertEquals(65535, NetUtils.MAX_PORT);
	}

}

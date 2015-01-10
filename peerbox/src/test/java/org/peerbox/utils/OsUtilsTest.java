package org.peerbox.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OsUtilsTest {

	@Test
	public void testWindows() {
		if (OsUtils.isWindows()) {
			boolean contains = OsUtils.getOsName().toLowerCase().contains("windows");
			assertTrue(contains);
		}
	}

	@Test
	public void testWindowsIsNotLinux() {
		if (OsUtils.isWindows()) {
			assertFalse(OsUtils.isLinux());
		}
	}

	@Test
	public void testLinux() {
		if (OsUtils.isLinux()) {
			boolean contains = OsUtils.getOsName().toLowerCase().contains("linux");
			assertTrue(contains);
		}
	}

	@Test
	public void testLinuxIsNotWindows() {
		if (OsUtils.isLinux()) {
			assertFalse(OsUtils.isWindows());
		}
	}
}

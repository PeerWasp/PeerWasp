package org.peerbox.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.peerbox.BaseJUnitTest;

public class OsUtilsTest extends BaseJUnitTest {

	@Test
	public void testWindows() {
		if (OsUtils.isWindows()) {
			boolean contains = OsUtils.getOsName().toLowerCase().contains("windows");
			assertTrue(contains);
		}
	}

	@Test
	public void testWindowsBooleans() {
		if (OsUtils.isWindows()) {
			assertFalse(OsUtils.isLinux());
			assertFalse(OsUtils.isOSX());
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
	public void testLinuxBooleans() {
		if (OsUtils.isLinux()) {
			assertFalse(OsUtils.isWindows());
			assertFalse(OsUtils.isOSX());
		}
	}

	@Test
	public void testOSX() {
		if (OsUtils.isOSX()) {
			boolean contains = OsUtils.getOsName().toLowerCase().contains("mac os");
			assertTrue(contains);
		}
	}

	@Test
	public void testOSXBooleans() {
		if (OsUtils.isOSX()) {
			assertFalse(OsUtils.isWindows());
			assertFalse(OsUtils.isLinux());
		}
	}
}

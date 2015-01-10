package org.peerbox.utils;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class OsUtilsTest {

	@Test
	public void testWindows() {
		if(OsUtils.isWindows()) {
			OsUtils.getOsName().toLowerCase().contains("windows");
		}
	}
	
	@Test
	public void testWindowsIsNotLinux() {
		if(OsUtils.isWindows()) {
			assertFalse(OsUtils.isLinux());
		}
	}
	
	@Test 
	public void testLinux() {
		if(OsUtils.isLinux()) {
			OsUtils.getOsName().toLowerCase().contains("linux");
		}
	}
	
	@Test
	public void testLinuxIsNotWindows() {
		if(OsUtils.isLinux()) {
			assertFalse(OsUtils.isWindows());
		}
	}
}

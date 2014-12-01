package org.peerbox.utils;

/**
 * Utility class for operating system related functions
 * 
 * @author albrecht
 *
 */
public final class OsUtils {

	/**
	 * @return the name of the OS
	 */
	public static String getOsName() {
		return System.getProperty("os.name");
	}

	/**
	 * @return true if current OS is Windows
	 */
	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	/**
	 * @return true if current OS is Linux
	 */
	public static boolean isLinux() {
		return getOsName().equalsIgnoreCase("Linux");
	}

	/**
	 * @return true if current OS is OS X
	 */
//	public static boolean isOSX() {
//		return getOsName().equalsIgnoreCase("Mac OS X");
//	}
}

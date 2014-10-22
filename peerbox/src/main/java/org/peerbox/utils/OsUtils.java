package org.peerbox.utils;

public final class OsUtils {
	   public static String getOsName()
	   {
	      return System.getProperty("os.name");
	   }
	   public static boolean isWindows()
	   {
	      return getOsName().startsWith("Windows");
	   }

	   public static boolean isLinux() {
		   return getOsName().equalsIgnoreCase("Linux");
	   }
	   
	   public static boolean isOSX() {
		   return getOsName().equalsIgnoreCase("Mac OS X");
	   }
}

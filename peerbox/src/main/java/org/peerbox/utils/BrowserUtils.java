package org.peerbox.utils;

/////////////////////////////////////////////////////////
// Bare Bones Browser Launch                           //
// Version 3.1 (June 6, 2010)                          //
// By Dem Pilafian                                     //
// Supports:                                           //
// Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7       //
// Example Usage:                                      //
// String url = "http://www.centerkey.com/";           //
// BareBonesBrowserLaunch.openURL(url);                //
// Public Domain Software -- Free to Use as You Like   //
/////////////////////////////////////////////////////////
// Source: http://www.centerkey.com/java/browser/      //
/////////////////////////////////////////////////////////

import java.io.IOException;
import java.util.Arrays;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Based on Bare Bones Browser Launch (see header).
 * Modifications: xdg-open added, minor refactoring
 *
 * @author albrecht
 *
 */
public final class BrowserUtils {

	private BrowserUtils() {
		// prevent instances
	}

	private static final String[] browsers = {
		"xdg-open",
		"google-chrome",
		"firefox",
		"opera",
		"epiphany",
		"konqueror",
		"conkeror",
		"midori",
		"kazehakase",
		"mozilla"
	};

	private static final String errMsg = "Error attempting to launch web browser";

	public static void openURL(String url) {

		try {

			// attempt to use Desktop library from JDK 1.6+
			Class<?> d = Class.forName("java.awt.Desktop");
			d.getDeclaredMethod("browse", new Class[] { java.net.URI.class }).invoke(
					d.getDeclaredMethod("getDesktop").invoke(null),
					new Object[] { java.net.URI.create(url) });
			// above code mimicks: java.awt.Desktop.getDesktop().browse()

		} catch (Exception ignore) {
			// library not available or failed
			try {
				if(OsUtils.isWindows()) {

					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);

				} else if(OsUtils.isOSX()) {

					Class.forName("com.apple.eio.FileManager")
						.getDeclaredMethod("openURL", new Class[] { String.class })
						.invoke(null, new Object[] { url });

				} else if(OsUtils.isLinux()) {

					String browser = null;
					for (String b : browsers) {
						if (browser == null && isExecAvailable(b))
							// open page
							Runtime.getRuntime().exec(new String[] { browser = b, url });
					}

					// no browser found
					if (browser == null) {
						throw new Exception("Could not open URL using browsers: " + Arrays.toString(browsers));
					}
				}

			} catch (Exception e) {
				Alert dlg = new Alert(AlertType.WARNING);
				dlg.setHeaderText(errMsg);
				dlg.setTitle(errMsg);
				dlg.setContentText(e.getMessage());
			}
		}
	}

	private static boolean isExecAvailable(String exec) throws IOException {
		Process p = Runtime.getRuntime().exec(new String[] { "which", exec });
		boolean isAvailable = p.getInputStream().read() != -1;
		return isAvailable;
	}

}

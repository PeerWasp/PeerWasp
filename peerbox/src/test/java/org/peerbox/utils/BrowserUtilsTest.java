package org.peerbox.utils;

/**
 * This class is not a JUnit test class.
 *
 * Try out whether opening URL works.
 *
 * @author albrecht
 *
 */
public class BrowserUtilsTest {

	private static final String urlToOpen = "http://www.peerwasp.com";

	public static void main(String[] args) {

		BrowserUtils.openURL(urlToOpen);

	}
}

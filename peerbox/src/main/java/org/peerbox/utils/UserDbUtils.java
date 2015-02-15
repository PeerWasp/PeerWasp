package org.peerbox.utils;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

public class UserDbUtils {

	private UserDbUtils() {
		// prevent instances
	}

	public static String createFileName(String username) {
		String usernameLower = username.toLowerCase();
		String usernameHash = hashString(usernameLower);
		String filename = String.format("%s.db", usernameHash);
		return filename;
	}

	private static String hashString(String str) {
		return Hashing.sha256().hashString(str, Charsets.UTF_8).toString();
	}

}

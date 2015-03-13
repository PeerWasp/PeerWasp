package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.hive2hive.core.security.HashUtil;

/**
 * This is a utility class to compute file content hashes and
 * to encode and decode using base64.
 * @author Claudio
 *
 */
public class PathUtils {

	/**
	 * This method computes the hash over a file. If the file
	 * is not accessible for some reason (i.e. locked by another
	 * process), then the method makes three consecutive tries
	 * after waiting 3 seconds.
	 *
	 * @param path
	 * @return the hash as base64 encoded string
	 */
	public static String computeFileContentHash(Path path) {
		String newHash = "";
		if (path != null && path.toFile() != null) {
			for (int i = 0; i < 3; i++) {
				try {
					byte[] rawHash = HashUtil.hash(path.toFile());
					if (rawHash != null) {
						newHash = base64Encode(rawHash);
					}
					break;
				} catch (IOException e) {
					e.printStackTrace();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return newHash;
	}

	/**
	 * Encodes the specified byte array into a String using the Base64 encoding scheme
	 *
	 * @param bytes
	 * @return base64 string
	 */
	public static String base64Encode(byte[] bytes) {
		String hashString = Base64.getEncoder().encodeToString(bytes);
		return hashString;
	}

	/**
	 * Decodes the specified String into a byte array assuming that the string is Base64 encoded.
	 *
	 * @param data
	 * @return decoded byte
	 */
	public static byte[] base64Decode(String data) {
		byte[] d = Base64.getDecoder().decode(data.getBytes());
		return d;
	}
	
	public static boolean isFileHidden(Path file){

		List<String> illegal = new ArrayList<String>();
		illegal.add("$");
		illegal.add(".");
		illegal.add("~");
		
		return illegal.stream().anyMatch(pattern -> file.getFileName().toString().startsWith(pattern));
		
	}
}

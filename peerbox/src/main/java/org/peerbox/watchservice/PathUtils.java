package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
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
	 * @param path to file on disk
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
	 * @param bytes to encode
	 * @return base64 string
	 */
	public static String base64Encode(byte[] bytes) {
		String hashString = Base64.getEncoder().encodeToString(bytes);
		return hashString;
	}

	/**
	 * Decodes the specified String into a byte array assuming that the string is Base64 encoded.
	 *
	 * @param data to decode
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

    public static Path getCommonPath(Path path1, Path path2){
		Path commonPath = Paths.get("");
    	if(path1 == null || path2 == null){
    		return commonPath;
    	}
		Iterator<Path> iterPath1 = path1.iterator();
		Iterator<Path> iterPath2 = path2.iterator();

		while(iterPath1.hasNext() && iterPath2.hasNext()){
			Path next1 = iterPath1.next();

			if(next1.equals(iterPath2.next())){
				commonPath = commonPath.resolve(next1);
			} else {
				break;
			}
		}

		return commonPath;
	}
}

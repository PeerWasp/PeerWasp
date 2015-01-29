package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;

import org.hive2hive.core.security.HashUtil;

public class PathUtils {

//	public static String getNextPathFragment(String path){
//
//		int index = path.indexOf(File.separator);
//		if(index == -1){
//			return path;
//		} else if(index == 0){
//			return getNextPathFragment(path.substring(1));
//		}
//		return path.substring(0, index);
//	}
//
//	public static String getRemainingPathFragment(String path) {
//		String remainingPath = path;
//		if(path.startsWith(File.separator)){
//			remainingPath = path.substring(1);
//		}
//		return remainingPath.substring(getNextPathFragment(path).length());
//	}

//	public static Path getRecoveredFilePath(String file, int version){
//		String fileWithoutExt = FilenameUtils.removeExtension(file);
//		String ext = FilenameUtils.getExtension(file);
//		return Paths.get(fileWithoutExt + "_v" + version + "_recovered." + ext);
//	}

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

	public static String base64Encode(byte[] bytes) {
		String hashString = Base64.getEncoder().encodeToString(bytes);
		return hashString;
	}

//	public static boolean deleteRecursively(File path) {
//		if (path.exists()) {
//			if(path.isDirectory()){
//				File[] files = path.listFiles();
//				for (int i = 0; i < files.length; i++) {
//					if (files[i].isDirectory()) {
//						deleteRecursively(files[i]);
//					} else {
//						files[i].delete();
//					}
//				}
//			}
//			return (path.delete());
//		}
//		return false;
//	}
}

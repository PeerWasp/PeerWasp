package org.peerbox.watchservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.io.FilenameUtils;
import org.hive2hive.core.security.HashUtil;

public class PathUtils {
	
	public static String getNextPathFragment(String path){
		
		int index = path.indexOf(File.separator);
		if(index == -1){
			return path;
		} else if(index == 0){
			return getNextPathFragment(path.substring(1));
		}
		return path.substring(0, index);
	}

	public static String getRemainingPathFragment(String path) {
		String remainingPath = path;
		if(path.startsWith(File.separator)){
			remainingPath = path.substring(1);
		}
		return remainingPath.substring(getNextPathFragment(path).length());
	}
	
	public static Path getRecoveredFilePath(String file, int version){
		String fileWithoutExt = FilenameUtils.removeExtension(file);
		String ext = FilenameUtils.getExtension(file);
		return Paths.get(fileWithoutExt + "_v" + version + "_recovered." + ext);
	}
	
	public static String computeFileContentHash(Path path){
		String newHash = "";
		if(path != null && path.toFile() != null){
			try {
				byte[] rawHash = HashUtil.hash(path.toFile());
				if(rawHash != null){
					newHash = PathUtils.createStringFromByteArray(rawHash);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return newHash;
	}
	
	public static String createStringFromByteArray(byte[] bytes){
		String hashString = Base64.getEncoder().encodeToString(bytes);
		return hashString;
	}
}

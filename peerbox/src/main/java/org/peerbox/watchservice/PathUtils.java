package org.peerbox.watchservice;

import java.io.File;

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
}

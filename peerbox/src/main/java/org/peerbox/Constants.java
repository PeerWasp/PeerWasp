package org.peerbox;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public interface Constants {
	
	public static final String APP_NAME = "PeerBox";
	//User password length
	public static final int MIN_PASSWORD_LENGTH = 6;
	
	//User PIN length
	public static final int MIN_PIN_LENGTH = 3;
	
	public static Path APP_DATA_FOLDER = Paths.get(FileUtils.getUserDirectoryPath(), APP_NAME);
	
	public static Path APP_CACHE_FOLDER = Paths.get(APP_DATA_FOLDER.toString(), "cache");
	
	public static Path APP_CONFIG_FOLDER = APP_DATA_FOLDER;
}

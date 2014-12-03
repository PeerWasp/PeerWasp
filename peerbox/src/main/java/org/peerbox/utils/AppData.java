package org.peerbox.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.peerbox.app.Constants;

public final class AppData {
	
	private AppData() {
		// prevent instances
	}
	
	private static final Path APP_DATA_FOLDER;
	
	private static final Path APP_CACHE_FOLDER;
	
	private static final Path APP_CONFIG_FOLDER;
	
	static {
		APP_DATA_FOLDER = Paths.get(FileUtils.getUserDirectoryPath(), ("." + Constants.APP_NAME));
		APP_CACHE_FOLDER = Paths.get("cache");
		APP_CONFIG_FOLDER = Paths.get("config");
	}
		
	public static Path getAppDataFolder() throws IOException {
		if(!Files.exists(APP_DATA_FOLDER)) {
			Files.createDirectories(APP_DATA_FOLDER);
		}
		Files.setAttribute(APP_DATA_FOLDER, "dos:hidden", true);
		return APP_DATA_FOLDER;
	}
	
	public static Path getConfigFolder() throws IOException {
		Path config = getAppDataFolder().resolve(APP_CONFIG_FOLDER);
		if(!Files.exists(config)) {
			Files.createDirectories(config);
		}
		return config;
	}
	
	public static Path getCacheFolder() throws IOException {
		Path cache = getAppDataFolder().resolve(APP_CACHE_FOLDER);
		if(!Files.exists(cache)) {
			Files.createDirectories(cache);
		}
		return cache;
	}
}

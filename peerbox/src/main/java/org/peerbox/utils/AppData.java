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
		APP_DATA_FOLDER = Paths.get(FileUtils.getUserDirectoryPath(),
				String.format(".%s", Constants.APP_NAME));

		APP_CACHE_FOLDER = APP_DATA_FOLDER.resolve("cache");

		APP_CONFIG_FOLDER = APP_DATA_FOLDER.resolve("config");
	}

	public static Path getDataFolder() {
		return APP_DATA_FOLDER;
	}

	public static Path getConfigFolder() {
		return APP_CONFIG_FOLDER;
	}

	public static Path getCacheFolder() {
		return APP_CACHE_FOLDER;
	}

	public static void createFolders() throws IOException {
		createDirectoriesIfNotExists(getDataFolder());
		Files.setAttribute(getDataFolder(), "dos:hidden", true);

		createDirectoriesIfNotExists(getConfigFolder());

		createDirectoriesIfNotExists(getCacheFolder());
	}

	private static void createDirectoriesIfNotExists(final Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
	}

	public static void checkAccess() throws IOException {
		ensureFolderWriteAccess(getDataFolder());
		ensureFolderWriteAccess(getConfigFolder());
		ensureFolderWriteAccess(getCacheFolder());
	}

	private static void ensureFolderWriteAccess(final Path path) throws IOException {
		if (!Files.isDirectory(path)) {
			throw new IOException("Path points not to a folder: " + path);
		}
		if (!Files.isWritable(path)) {
			throw new IOException("No write access to path: " + path);
		}
	}
}

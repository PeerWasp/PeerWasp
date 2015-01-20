package org.peerbox.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.peerbox.app.Constants;

/**
 * Application data utility class.
 *
 * @author albrecht
 *
 */
public final class AppData {

	private AppData() {
		// prevent instances
	}

	private static Path DATA_FOLDER_PATH;

	private static final Path CACHE_FOLDER_NAME;

	private static final Path CONFIG_FOLDER_NAME;

	private static final Path LOG_FOLDER_NAME;

	static {
		// default location
		DATA_FOLDER_PATH = Paths.get(FileUtils.getUserDirectoryPath(),
				String.format(".%s", Constants.APP_NAME));

		CACHE_FOLDER_NAME = Paths.get("cache");

		CONFIG_FOLDER_NAME = Paths.get("config");

		LOG_FOLDER_NAME = Paths.get("log");
	}

	public static void setDataFolder(final Path folder) {
		DATA_FOLDER_PATH = folder;
	}

	public static Path getDataFolder() {
		return DATA_FOLDER_PATH;
	}

	public static Path getConfigFolder() {
		return getDataFolder().resolve(CONFIG_FOLDER_NAME);
	}

	public static Path getCacheFolder() {
		return getDataFolder().resolve(CACHE_FOLDER_NAME);
	}

	public static Path getLogFolder() {
		return getDataFolder().resolve(LOG_FOLDER_NAME);
	}

	public static void createFolders() throws IOException {
		createDirectoriesIfNotExists(getDataFolder());
		Files.setAttribute(getDataFolder(), "dos:hidden", true);

		createDirectoriesIfNotExists(getConfigFolder());

		createDirectoriesIfNotExists(getCacheFolder());

		createDirectoriesIfNotExists(getLogFolder());
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
		ensureFolderWriteAccess(getLogFolder());
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

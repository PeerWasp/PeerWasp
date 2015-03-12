package org.peerbox.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.TreeMap;

import org.peerbox.app.config.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

/**
 * Utilities class for loading user config files and creating instances to use.
 *
 * @author albrecht
 *
 */
public class UserConfigUtils {

	private static final Logger logger = LoggerFactory.getLogger(UserConfigUtils.class);

	private UserConfigUtils() {
		// prevent instances
	}

	/**
	 * Creates a user config instance given a username.
	 * The config file will be searched or created in the {@link AppData#getConfigFolder()} folder.
	 *
	 * @param username
	 * @return user config
	 */
	public static UserConfig createUserConfig(String username) {
		String filename = createFileName(username);
		Path file = AppData.getConfigFolder().resolve(filename);
		UserConfig cfg = createUserConfig(file);
		return cfg;
	}

	/**
	 * Creates a user config instance given a path to a file.
	 *
	 * @param file
	 * @return
	 */
	public static UserConfig createUserConfig(Path file) {
		UserConfig cfg = new UserConfig(file);
		return cfg;
	}

	private static String createFileName(String username) {
		String usernameLower = username.toLowerCase();
		String usernameHash = hashString(usernameLower);
		String filename = String.format("%s.conf", usernameHash);
		return filename;
	}

	private static String hashString(String str) {
		return Hashing.sha256().hashString(str, Charsets.UTF_8).toString();
	}

	/**
	 * Searches for user config files in the {@link AppData#getConfigFolder()} folder.
	 * All found config files are loaded into a {@link UserConfig} instance and returned.
	 *
	 * @return map mapping username to the user config.
	 */
	public static Map<String, UserConfig> getAllConfigFiles() {
		return getAllConfigFiles(AppData.getConfigFolder());
	}

	private static Map<String, UserConfig> getAllConfigFiles(Path configFolder) {
		Map<String, UserConfig> userToFile = new TreeMap<>();

		try {

			DirectoryStream<Path> dirStream = Files.newDirectoryStream(configFolder,
					new DirectoryStream.Filter<Path>() {
						PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.conf");
						@Override
						public boolean accept(Path entry) throws IOException {
							return Files.isRegularFile(entry) && matcher.matches(entry.getFileName());
						}
					});

			for (Path configFile : dirStream) {
				UserConfig cfg = new UserConfig(configFile);
				cfg.load();
				if (cfg.hasUsername()) {
					String username = cfg.getUsername();
					userToFile.put(username, cfg);
				}
			}

		} catch (IOException e) {
			logger.warn("Could not search for config files.", e);
			userToFile.clear();
		}

		return userToFile;
	}

}

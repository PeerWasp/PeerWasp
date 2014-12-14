package org.peerbox.h2h;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.core.file.IFileAgent;

public final class FileAgent implements IFileAgent {
	
	private Path root;
	private Path cache;

	public FileAgent(final Path root, final Path cache) {
		this.root = root;
		this.cache = cache;
	}

	@Override
	public File getRoot() {
		return root.toFile();
	}

	@Override
	public void writeCache(String key, byte[] data) throws IOException {
		if (cache == null) {
			return;
		}
		if (data == null) {
			return;
		}

		ensureCacheDirExists();
		Path cacheFile = cache.resolve(key);
		try {
			Files.write(cacheFile, data);
		} catch (IOException e) {
			// could not write
		}
	}

	@Override
	public byte[] readCache(String key) {
		if (cache == null) {
			return null;
		}

		Path cacheFile = cache.resolve(key);
		if (!Files.exists(cacheFile)) {
			return null;
		}

		try {
			return Files.readAllBytes(cacheFile);
		} catch (IOException e) {
			// could not read
			return null;
		}
	}

	private void ensureCacheDirExists() throws IOException {
		if (!Files.exists(cache)) {
			Files.createDirectories(cache);
		}
	}
}

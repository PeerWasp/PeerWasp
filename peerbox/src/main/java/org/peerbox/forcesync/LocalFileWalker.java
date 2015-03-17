package org.peerbox.forcesync;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.hive2hive.core.security.HashUtil;
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.watchservice.PathUtils;

class LocalFileWalker extends SimpleFileVisitor<Path> {
	private Map<Path, FileInfo> local;

	public LocalFileWalker(Map<Path, FileInfo> resultMap) {
		this.local = resultMap;
	}

	public Map<Path, FileInfo> getLocalMap() {
		return local;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		FileInfo a = new FileInfo(dir, true);
		local.put(a.getPath(), a);
		return super.preVisitDirectory(dir, attrs);
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if(PathUtils.isFileHidden(file)){
			return FileVisitResult.CONTINUE;
		}
		
		FileInfo a = new FileInfo(file, false);
		String hash = hashFile(file);
		a.setContentHash(hash);
		local.put(a.getPath(), a);
		return super.visitFile(file, attrs);
	}

	private String hashFile(Path path) throws IOException {
		byte[] hash = HashUtil.hash(path.toFile());
		String hash64 = PathUtils.base64Encode(hash);
		return hash64;
	}
}

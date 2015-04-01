package org.peerbox.watchservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.peerbox.BaseJUnitTest;


public class PathUtilsTest extends BaseJUnitTest {

	@Test
	public void testComputeFileContentHash() throws IOException {
		Path file = null;
		try {
			file = Paths.get(FileUtils.getTempDirectoryPath(),
					RandomStringUtils.randomAlphanumeric(24));
			String content = "hello world";
			String md5AsBase64 = "XrY7u+Ae7tCTyyK7j1rNww==";

			Files.write(file, content.getBytes());
			String hash = PathUtils.computeFileContentHash(file);
			assertEquals(md5AsBase64, hash);
		} finally {
			if (file != null) {
				Files.deleteIfExists(file);
			}
		}
	}

	@Test
	public void testComputeFileContentHash_Null() throws IOException {
		String hash = PathUtils.computeFileContentHash(null);
		assertEquals(hash, "");
	}

	@Test
	public void testComputeFileContentHash_EmptyFile() throws IOException {
		Path file = null;
		try {
			file = Paths.get(FileUtils.getTempDirectoryPath(),
					RandomStringUtils.randomAlphanumeric(24));
			Files.createFile(file);
			String hash = PathUtils.computeFileContentHash(file);
			String base64_md5 = "1B2M2Y8AsgTpgAmY7PhCfg==";
			assertEquals(base64_md5, hash);
		} finally {
			if (file != null) {
				Files.deleteIfExists(file);
			}
		}
	}

	@Test
	public void testBase64Encode() {
		String content = "hello world";
		String expected = "aGVsbG8gd29ybGQ=";

		String encoded = PathUtils.base64Encode(content.getBytes());
		assertEquals(encoded, expected);
	}

	@Test
	public void testBase64Decode() {
		String encoded = "aGVsbG8gd29ybGQ=";
		String expected = "hello world";

		byte[] decoded = PathUtils.base64Decode(encoded);
		String content = new String(decoded);
		assertTrue(content.equals(expected));
	}

	@Test
	public void isFileHiddenTest(){
		assertTrue(PathUtils.isFileHidden(Paths.get(".file")));
		assertTrue(PathUtils.isFileHidden(Paths.get("~file")));
		assertTrue(PathUtils.isFileHidden(Paths.get("$file")));
		assertFalse(PathUtils.isFileHidden(Paths.get("file")));
	}

	@Test
	public void getCommonPathTest(){
		Path emptyPath = Paths.get("");
		Path dir1file1 = Paths.get("dir1/file1");
		Path dir1dir2file1 = Paths.get("dir1/dir2/file1");
		Path dir1dir2file2 = Paths.get("dir1/dir2/file2");
		Path dir1dir3 = Paths.get("dir1/dir3");

		Path result = PathUtils.getCommonPath(null, dir1file1);
		assertEquals(result.toString(), "");

		result = PathUtils.getCommonPath(emptyPath, dir1file1);
		assertEquals(result.toString(), "");

		result = PathUtils.getCommonPath(dir1file1, dir1dir2file1);
		assertEquals(result.toString(), "dir1");

		result = PathUtils.getCommonPath(dir1file1, dir1dir3);
		assertEquals(result.toString(), "dir1");

		result =  PathUtils.getCommonPath(dir1dir2file1, dir1dir2file2);
		assertEquals(result, Paths.get("dir1", "dir2"));
	}


}

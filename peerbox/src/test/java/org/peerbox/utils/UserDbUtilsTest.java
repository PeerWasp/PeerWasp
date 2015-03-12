package org.peerbox.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.peerbox.app.DbContext;

public class UserDbUtilsTest {

	@Test
	public void testCreateDbContext() throws IOException, SQLException {
		Path file = null;
		try {
			file = Paths.get(FileUtils.getTempDirectoryPath(), "testdb_file.db");
			DbContext context = UserDbUtils.createDbContext(file);
			assertEquals(file, context.getDatabaseFile());
			assertNotNull(context.getDataSource());
			// check connection string.
			DatabaseMetaData dmd = context.getDataSource().getConnection().getMetaData();
			String url = dmd.getURL();
			assertTrue(url.contains(file.toString()));
		} finally {
			if (file != null) {
				Files.deleteIfExists(file);
			}
		}
	}

	@Test
	public void testCreateFileName() {
		String sha256 = "96d9632f363564cc3032521409cf22a852f2032eec099ed5967c0d000cec607a"; // John
		String username = "John";
		String filename = UserDbUtils.createFileName(username);
		String expected = String.format("%s.db", sha256);

		assertEquals(expected, filename);

		// lowercase
		filename = UserDbUtils.createFileName(username.toLowerCase());
		assertEquals(expected, filename);

		// uppercase
		filename = UserDbUtils.createFileName(username.toUpperCase());
		assertEquals(expected, filename);
	}

}

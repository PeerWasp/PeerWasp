package org.peerbox.utils;

import java.nio.file.Paths;

import javax.sql.DataSource;

import org.peerbox.DbContext;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class UserDbUtils {

	private UserDbUtils() {
		// prevent instances
	}

	/**
	 * Creates a database context with associated data source (connection pool) and database file.
	 *
	 * @param dbPath path to the db file
	 * @return configured data source
	 */
	public static DbContext createDbContext(final String dbPath) {
		DataSource dataSource = createDataSource(dbPath);
		DbContext dbContext = new DbContext();
		dbContext.setDataSource(dataSource);
		dbContext.setDatabaseFile(Paths.get(dbPath));
		return dbContext;
	}

	private static DataSource createDataSource(final String dbPath) {
		HikariConfig hikariConfig = createConnectionPoolConfig(dbPath);
		DataSource dataSource = new HikariDataSource(hikariConfig);
		return dataSource;
	}

	private static HikariConfig createConnectionPoolConfig(final String dbPath) {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(String.format("jdbc:h2:%s", dbPath));
		hikariConfig.setUsername("sa");
		// hikariConfig.setPassword("");
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
		return hikariConfig;
	}

	/**
	 * Creates a filename for a database given a username.
	 *
	 * @param username for which db file should be created
	 * @return filename (not a full path)
	 */
	public static String createFileName(String username) {
		String usernameLower = username.toLowerCase();
		String usernameHash = hashString(usernameLower);
		String filename = String.format("%s.db", usernameHash);
		return filename;
	}

	private static String hashString(String str) {
		return Hashing.sha256().hashString(str, Charsets.UTF_8).toString();
	}

}

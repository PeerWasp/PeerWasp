package org.peerbox.watchservice.filetree.persistency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

/**
 * Utilities for database access (DAO classes).
 *
 * @author albrecht
 *
 */
public class DaoUtils {

	private DaoUtils() {
		// prevent instances
	}

	/**
	 * Dumps the content of a table to a csv file.
	 *
	 * @param tableName the table name to dump
	 * @param sql2o initialized database access
	 */
	public static void dumpTableToCsv(final String tableName, final Sql2o sql2o) {
		Path file = createFileForTable(tableName);
		String selectQuery = createQueryForTable(tableName);
		dumpTableToCsv(file, selectQuery, sql2o);
	}

	private static void dumpTableToCsv(final Path file, final String selectQuery, final Sql2o sql2o) {
		final String sql =
				"CALL "
				+ "CSVWRITE( "
				+ String.format("'%s', ", file.toString())
				+ String.format("'%s', 'charset=UTF-8 fieldSeparator=;' ", selectQuery)
				+ " );";

		try (Connection con = sql2o.open()) {
			con.createQuery(sql).executeUpdate();
		}
	}

	private static Path createFileForTable(final String tableName) {
		long ts = System.currentTimeMillis();
		Date date = new Date(ts);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String fileName = String.format("%s-%s.csv", tableName, sdf.format(date));
		Path file = Paths.get(fileName);
		return file;
	}

	private static String createQueryForTable(final String tableName) {
		String sql = String.format("SELECT * FROM %s;", tableName);
		return sql;
	}

}

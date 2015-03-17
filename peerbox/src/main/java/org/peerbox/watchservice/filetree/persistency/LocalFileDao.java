package org.peerbox.watchservice.filetree.persistency;

import java.nio.file.Path;
import java.util.List;

import org.peerbox.app.DbContext;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.google.inject.Inject;


public class LocalFileDao {

	private static final String FILE_TABLE = "files";

	private static final String DEFAULT_COLUMNS = "id, path, is_file, content_hash, "
												+ "is_synchronized, is_uploaded, "
												+ "current_state, next_state";

	private final Sql2o sql2o;
	private final DbContext dbContext;

	@Inject
	public LocalFileDao(DbContext dbContext) {
		this.dbContext = dbContext;
		this.sql2o = new Sql2o(this.dbContext.getDataSource());

		createTable();
	}

	public void createTable() {
		final String tableSql =
				"CREATE TABLE IF NOT EXISTS " + FILE_TABLE + " ( "
					    + "id IDENTITY NOT NULL PRIMARY KEY auto_increment, "
					    + "path NVARCHAR NOT NULL UNIQUE, "
					    + "is_file BOOLEAN NOT NULL, "
					    + "content_hash NVARCHAR(64) NOT NULL, "
					    + "is_synchronized BOOLEAN NOT NULL DEFAULT(false), "
					    + "is_uploaded BOOLEAN NOT NULL DEFAULT(false), "
					    + "current_state NVARCHAR(32), "
					    + "next_state NVARCHAR(32), "
					    + "to_delete BOOLEAN NOT NULL DEFAULT(false) "
				+ ");";


			final String indexSql =
					"CREATE UNIQUE INDEX IF NOT EXISTS files_path "
					+ "ON "+ FILE_TABLE + " (path);";

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery(tableSql).executeUpdate();
			con.createQuery(indexSql).executeUpdate();
			con.commit();
		}
	}

	public List<FileComponent> getAllFiles() {
		final String sql = String.format(
				"SELECT %s FROM %s ORDER BY path ASC;", DEFAULT_COLUMNS, FILE_TABLE);

		try (Connection con = sql2o.open()) {
			return con.createQuery(sql)
					.executeAndFetch(new FileComponentResultSetHandler());
		}
	}

	public FileComponent getFileByPath(final Path path) {
		final String sql = String.format(
				"SELECT %s FROM %s WHERE path = :path;", DEFAULT_COLUMNS, FILE_TABLE);

		try (Connection con = sql2o.open()) {
			return con.createQuery(sql)
					.addParameter("path", path.toString())
					.executeAndFetchFirst(new FileComponentResultSetHandler());
		}
	}

	public void deleteByPath(final Path file) {
		final String sql = String.format(
				"DELETE FROM %s WHERE path = :path", FILE_TABLE);

		try (Connection con = sql2o.open()) {
			con.createQuery(sql)
				.addParameter("path", file.toString())
				.executeUpdate();
		}
	}

	public void persistFile(final FileComponent file) {
		final String sql =
				"MERGE INTO " + FILE_TABLE + " "
				+ "(path, is_file, content_hash, is_synchronized, is_uploaded, current_state, next_state, to_delete) "
				+ "KEY(path) "
				+ "VALUES (:path, :is_file, :content_hash, :is_synchronized, :is_uploaded, :current_state, :next_state, :to_delete);";

		try (Connection con = sql2o.open()) {
			con.createQuery(sql)
				.addParameter("path", file.getPath().toString())
				.addParameter("is_file", file.isFile())
				.addParameter("content_hash", file.getContentHash())
				.addParameter("is_synchronized", file.isSynchronized())
				.addParameter("is_uploaded", file.isUploaded())
				.addParameter("current_state", file.getAction().getCurrentState().getStateType().toString())
				.addParameter("next_state", file.getAction().getNextState().getStateType().toString())
				.addParameter("to_delete", false)
				.executeUpdate();
		}
	}

	public void dumpCsv() {
		DaoUtils.dumpTableToCsv(FILE_TABLE, sql2o);
	}

	public void persistAndReplaceFileComponents(List<FileComponent> files) {
		final String markToDelete = String.format(
				"UPDATE %s SET to_delete = true;", FILE_TABLE);

		final String deleteStale = String.format(
				"DELETE FROM %s WHERE to_delete = true", FILE_TABLE);

		final String sql =
				"MERGE INTO " + FILE_TABLE + " "
				+ "(path, is_file, content_hash, is_synchronized, is_uploaded, current_state, next_state, to_delete) "
				+ "KEY(path) "
				+ "VALUES (:path, :is_file, :content_hash, :is_synchronized, :is_uploaded, :current_state, :next_state, :to_delete);";

		try (Connection con = sql2o.beginTransaction()) {
			// mark to delete
			con.createQuery(markToDelete).executeUpdate();

			for (FileComponent file : files) {
				// insert or update
				con.createQuery(sql)
					.addParameter("path", file.getPath().toString())
					.addParameter("is_file", file.isFile())
					.addParameter("content_hash", file.getContentHash())
					.addParameter("is_synchronized", file.isSynchronized())
					.addParameter("is_uploaded", file.isUploaded())
					.addParameter("current_state", file.getAction().getCurrentState().getStateType().toString())
					.addParameter("next_state", file.getAction().getNextState().getStateType().toString())
					.addParameter("to_delete", false)
					.executeUpdate();
			}

			// cleanup deleted files
			con.createQuery(deleteStale).executeUpdate();

			con.commit();
		}
	}

	public Boolean isSynchronizedByPath(Path path) {
		final String sql = String.format(
				"SELECT is_synchronized isSynchronized FROM %s WHERE path = :path", FILE_TABLE);

		Boolean isSync = null;
		try(Connection con = sql2o.open()) {
			isSync = con.createQuery(sql)
					.addParameter("path", path.toString())
					.executeScalar(Boolean.class);
		}
		return isSync;
	}
}

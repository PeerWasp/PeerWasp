package org.peerbox.watchservice.filetree.persistency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import com.google.inject.name.Named;


public class FileDao {

	private static final String FILE_TABLE = "files";

	private static final String DEFAULT_COLUMNS = "id, path, is_file, content_hash, "
												+ "is_synchronized, is_uploaded, "
												+ "current_state, next_state";

	private final Sql2o sql2o;
	private final DataSource dataSource;

	public FileDao(@Named("userdb") DataSource dataSource) {
		this.dataSource = dataSource;
		this.sql2o = new Sql2o(this.dataSource);
	}

	public void createTable() {
		final String tableSql =
				"CREATE TABLE IF NOT EXISTS " + FILE_TABLE + " ( "
					    + "id IDENTITY NOT NULL PRIMARY KEY auto_increment, "
					    + "path NVARCHAR NOT NULL UNIQUE, "
					    + "is_file BOOLEAN NOT NULL, "
					    + "content_hash NVARCHAR(64) NOT NULL, "
					    + "execute_content_hash NVARCHAR(64), "
					    + "is_synchronized BOOLEAN NOT NULL DEFAULT(false), "
					    + "is_uploaded BOOLEAN NOT NULL DEFAULT(false), "
					    + "current_state NVARCHAR(32), "
					    + "next_state NVARCHAR(32) "
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

	public void persistFile(final FileComponent file) {
		final String sql =
				"MERGE INTO " + FILE_TABLE + " "
				+ "(path, is_file, content_hash, is_synchronized, is_uploaded, current_state, next_state) "
				+ "VALUES (:path, :is_file, :content_hash, :is_synchronized, :is_uploaded, :current_state, :next_state);";

		try (Connection con = sql2o.open()) {
			con.createQuery(sql)
				.addParameter("path", file.getPath().toString())
				.addParameter("is_file", file.isFile())
				.addParameter("content_hash", file.getContentHash())
				.addParameter("is_synchronized", file.isSynchronized())
				.addParameter("is_uploaded", file.isUploaded())
				.addParameter("current_state", file.getAction().getCurrentState().getStateType().toString())
				.addParameter("next_state", file.getAction().getNextState().getStateType().toString())
				.executeUpdate();
		}
	}

	public void dumpCsv() {
		DaoUtils.dumpTableToCsv(FILE_TABLE, sql2o);
	}

	private class FileComponentResultSetHandler implements ResultSetHandler<FileComponent> {

		@Override
		public FileComponent handle(ResultSet resultSet) throws SQLException {

			FileComponent f = null;

			final Path path = Paths.get(resultSet.getString("path"));
			final boolean isFile = resultSet.getBoolean("is_file");
			final String contentHash = resultSet.getString("content_hash");
			final boolean isSynchronized = resultSet.getBoolean("is_synchronized");
			final boolean isUploaded = resultSet.getBoolean("is_uploaded");

			if (isFile) {
				f = new FileLeaf(path, true);
			} else {
				f = new FolderComposite(path, true, true);
			}

			f.setContentHash(contentHash);
			f.setIsSynchronized(isSynchronized);
			f.setIsUploaded(isUploaded);

			return f;
		}
	}
}

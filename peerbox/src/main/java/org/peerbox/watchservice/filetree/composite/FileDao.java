package org.peerbox.watchservice.filetree.composite;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class FileDao {

	private static final String FILE_TABLE = "files";

	private Sql2o sql2o = null;

	public FileDao() {

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl("jdbc:h2:~/userDB");
		hikariConfig.setUsername("sa");
		// hikariConfig.setPassword("");
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

		DataSource dataSource = new HikariDataSource(hikariConfig);

		sql2o = new Sql2o(dataSource);
	}



	public static void main(String[] args) {
		FileDao dao = new FileDao();
		dao.createTable();

		Path p = Paths.get("/tmp/pbx/test.txt");
		FileComponent f = dao.getFileByPath(p);
		System.out.println(f);

		FileComponent f2 = new FileLeaf(p, false);
		dao.persistFile(f2);

		f = dao.getFileByPath(p);
		System.out.println(f);

		dao.dumpCsv();

	}

	public void createTable() {
		final String tableSql =
			"CREATE TABLE IF NOT EXISTS " + FILE_TABLE + " ( "
				    + "id IDENTITY NOT NULL PRIMARY KEY auto_increment, "
				    + "path NVARCHAR NOT NULL UNIQUE, "
				    + "is_file BOOLEAN NOT NULL, "
				    + "content_hash NVARCHAR(64) NOT NULL, "
				    + "is_synchronized BOOLEAN NOT NULL DEFAULT(FALSE), "
				    + "is_uploaded BOOLEAN NOT NULL DEFAULT(false)"
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

	public List<FileComponent> getFiles() {
		final String sql =
				"SELECT id, path, is_file, content_hash, is_synchronized, is_uploaded "
				+ "FROM " + FILE_TABLE + " "
				+ "ORDER BY path ASC;";

		try (Connection con = sql2o.open()) {
			return con.createQuery(sql)
					.executeAndFetch(FileComponent.class);
		}
	}


	public FileComponent getFileByPath(final Path path) {
		final String sql =
				"SELECT * "
				+ "FROM " + FILE_TABLE + " "
				+ "WHERE path = :path;";

		try (Connection con = sql2o.open()) {
			return con.createQuery(sql)
					.addParameter("path", path.toString())
					.executeAndFetchFirst(new FileComponentResultSetHandler());
//					.executeAndFetchFirst(FileComponent.class);
		}
	}

	private class FileComponentResultSetHandler implements ResultSetHandler<FileComponent> {

		@Override
		public FileComponent handle(ResultSet resultSet) throws SQLException {
			FileComponent f = null;

			final long id = resultSet.getLong("id");
			final Path path = Paths.get(resultSet.getString("path"));
			final boolean isFile = resultSet.getBoolean("is_file");
			final String contentHash = resultSet.getString("content_hash");
			final boolean isSynchronized = resultSet.getBoolean("is_synchronized");
			final boolean isUploaded = resultSet.getBoolean("is_uploaded");

			if(isFile) {
				f = new FileLeaf(path, false);
			} else {
				f = new FolderComposite(path, false, false);
			}

			f.setId(id);
			f.setContentHash(contentHash);
			f.setIsSynchronized(isSynchronized);
			f.setIsUploaded(isUploaded);

			return f;
		}

	}

	public void persistFile(final FileComponent file) {
		// if id already present, we loaded it from the DB.
		if (file.getId() > 0L) {
			updateFile(file);
		} else {
			insertFile(file);
		}
	}

	private void insertFile(final FileComponent file) {
		final String sql =
				"INSERT INTO " + FILE_TABLE + " "
				+ "(path, is_file, content_hash, is_synchronized, is_uploaded) "
				+ "VALUES (:path, :is_file, :content_hash, :is_synchronized, :is_uploaded);";

		try (Connection con = sql2o.open()) {
			long insertedId = (long) con.createQuery(sql, true)
					.addParameter("path", file.getPath().toString())
					.addParameter("is_file", file.isFile())
					.addParameter("content_hash", file.getContentHash())
					.addParameter("is_synchronized", file.isSynchronized())
					.addParameter("is_uploaded", file.isUploaded())
					.executeUpdate()
					.getKey();
			file.setId(insertedId);
		}

	}

	private void updateFile(final FileComponent file) {
		final String sql =
				"UPDATE " + FILE_TABLE + " "
				+ "SET path = :path, "
			    + "is_file = :is_file "
				+ "content_hash = :content_hash, "
				+ "is_synchronized = :is_synchronized, "
				+ "is_uploaded = :is_uploaded "
				+ "WHERE id = :id";

		try (Connection con = sql2o.open()) {
			con.createQuery(sql)
				.addParameter("path", file.getPath().toString())
				.addParameter("is_file", file.isFile())
				.addParameter("content_hash", file.getContentHash())
				.addParameter("is_synchronized", file.isSynchronized())
				.addParameter("is_uploaded", file.isUploaded())
				.addParameter("id", file.getId())
				.executeUpdate();

		}
	}

	public void dumpCsv() {
		final String sql =
				"CALL "
				+ "CSVWRITE( "
				+ String.format("'%s.csv', ", FILE_TABLE)
				+ String.format("'SELECT * FROM %s', 'charset=UTF-8 fieldSeparator=;'", FILE_TABLE)
				+ " );";

		try (Connection con = sql2o.open()) {
			con.createQuery(sql).executeUpdate();
		}

	}
}

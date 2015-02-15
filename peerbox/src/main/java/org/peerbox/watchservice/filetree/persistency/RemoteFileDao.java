package org.peerbox.watchservice.filetree.persistency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.DbContext;
import org.peerbox.watchservice.PathUtils;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.google.inject.Inject;

public class RemoteFileDao {

	private static final String REMOTE_FILE_TABLE = "remote_files";

	/* aliases are important to match Java bean */
	private static final String DEFAULT_COLUMNS = "path, is_file isFile, content_hash contentHash";

	private final DbContext dbContext;
	private final Sql2o sql2o;

	@Inject
	public RemoteFileDao(DbContext dbContext) {
		this.dbContext = dbContext;
		this.sql2o = new Sql2o(this.dbContext.getDataSource());
	}

	/**
	 * Creates tables and indices (if they do not exist yet)
	 */
	public void createTable() {
		final String tableSql =
				"CREATE TABLE IF NOT EXISTS " + REMOTE_FILE_TABLE + " ( "
						+ "id IDENTITY NOT NULL PRIMARY KEY auto_increment, "
						+ "path NVARCHAR NOT NULL UNIQUE, "
						+ "is_file BOOLEAN NOT NULL, "
						+ "content_hash NVARCHAR(64) NOT NULL, "
						+ "to_delete BOOLEAN NOT NULL DEFAULT(FALSE)"
				+ ");";

		final String indexSql =
				"CREATE UNIQUE INDEX IF NOT EXISTS remote_files_path "
				+ "ON "+ REMOTE_FILE_TABLE + " (path);";


		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery(tableSql).executeUpdate();
			con.createQuery(indexSql).executeUpdate();
			con.commit();
		}
	}

	/**
	 * Persists the given list of file nodes by either inserting new files or updating existing
	 * information.
	 *
	 * @param fileNodes
	 */
	public void persistAndReplaceFileNodes(List<FileNode> fileNodes) {

		/*
		 * procedure works as follows (in a transaction):
		 * - mark all records as to_delete(true)
		 * - update or insert records with given list, set to_delete(false)
		 * - delete all records that still have to_delete(true), i.e. records that were not updated
		 */

		final String markToDelete = String.format(
				"UPDATE %s SET to_delete = true;", REMOTE_FILE_TABLE);

		final String deleteStale = String.format(
				"DELETE FROM %s WHERE to_delete = true", REMOTE_FILE_TABLE);

		final String insert = String.format(
				"MERGE INTO %s (path, is_file, content_hash, to_delete) "
				+ "KEY (path) "
				+ "VALUES ( :path, :is_file, :content_hash, :to_delete );", REMOTE_FILE_TABLE);



		try (Connection con = sql2o.beginTransaction()) {
			// mark to delete
			con.createQuery(markToDelete).executeUpdate();

			for (FileNode node : fileNodes) {
				String hash = node.isFile() ? PathUtils.base64Encode(node.getMd5()) : "";
				// insert or update
				con.createQuery(insert)
						.addParameter("path", node.getFile().toString())
						.addParameter("is_file", node.isFile())
						.addParameter("content_hash", hash)
						.addParameter("to_delete", false)
						.executeUpdate();
			}

			// cleanup deleted files
			con.createQuery(deleteStale).executeUpdate();

			con.commit();
		}
	}

	public List<FileNodeAttr> getFileNodeAttributes() {
		final String sql = String.format(
				"SELECT %s FROM %s ORDER BY path ASC;", DEFAULT_COLUMNS, REMOTE_FILE_TABLE);

		try (Connection con = sql2o.open()) {
			return con.createQuery(sql).executeAndFetch(FileNodeAttr.class);
		}
	}

	public void dumpCsv() {
		DaoUtils.dumpTableToCsv(REMOTE_FILE_TABLE, sql2o);
	}

	public class FileNodeAttr {
		private Path path;
		private boolean isFile;
		private String contentHash;

		public Path getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = Paths.get(path);
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public boolean isFile() {
			return isFile;
		}

		public void setFile(boolean isFile) {
			this.isFile = isFile;
		}

		public String getContentHash() {
			return contentHash;
		}

		public void setContentHash(String contentHash) {
			this.contentHash = contentHash;
		}
	}

}

package org.peerbox.watchservice.filetree.persistency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.peerbox.app.manager.file.FileInfo;
import org.sql2o.ResultSetHandler;

/**
 * This result set handler converts database records (ResultSet) into
 * FileInfo objects.
 *
 * @author albrecht
 *
 */
class FileInfoResultSetHandler implements ResultSetHandler<FileInfo> {

	@Override
	public FileInfo handle(ResultSet resultSet) throws SQLException {

		final Path path = Paths.get(resultSet.getString("path"));
		final boolean isFile = resultSet.getBoolean("is_file");
		final String contentHash = resultSet.getString("content_hash");

		FileInfo fileInfo = new FileInfo(path, !isFile, contentHash);
		return fileInfo;
	}

}

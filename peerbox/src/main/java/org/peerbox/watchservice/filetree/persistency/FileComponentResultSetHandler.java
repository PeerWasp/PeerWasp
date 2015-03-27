package org.peerbox.watchservice.filetree.persistency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FileLeaf;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.sql2o.ResultSetHandler;

/**
 * This result set handler converts database records (ResultSet) into FolderComponents.
 * Depending on the is_file attribute either a FileLeaf or a FolderComposite is returned.
 *
 * @author albrecht
 *
 */
class FileComponentResultSetHandler implements ResultSetHandler<FileComponent> {

	@Override
	public FileComponent handle(ResultSet resultSet) throws SQLException {

		FileComponent f = null;

		final Path path = Paths.get(resultSet.getString("path"));
		final boolean isFile = resultSet.getBoolean("is_file");
		final String contentHash = resultSet.getString("content_hash");
		final boolean isSynchronized = resultSet.getBoolean("is_synchronized");
		final boolean isUploaded = resultSet.getBoolean("is_uploaded");

		if (isFile) {
			f = new FileLeaf(path, true, contentHash);
		} else {
			f = new FolderComposite(path, true, contentHash);
		}

		f.setIsSynchronized(isSynchronized);
		f.setIsUploaded(isUploaded);

		return f;
	}


}

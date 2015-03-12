package org.peerbox.app;

import java.nio.file.Path;

import javax.sql.DataSource;

/**
 * The DbContext class provides a data source to use by JDBC and a filename
 * for the database.
 *
 * @author albrecht
 *
 */
public class DbContext {

	private DataSource dataSource;
	private Path databaseFile;

	/**
	 * The data source
	 *
	 * @return datasource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the data source.
	 *
	 * @param dataSource
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Returns the filename of the database
	 *
	 * @return
	 */
	public Path getDatabaseFile() {
		return databaseFile;
	}

	/**
	 * Sets the database filename
	 *
	 * @param databaseFile
	 */
	public void setDatabaseFile(Path databaseFile) {
		this.databaseFile = databaseFile;
	}

}

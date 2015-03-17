package org.peerbox.app;

import java.nio.file.Path;

import javax.sql.DataSource;

/**
 * The DbContext class provides a data source to use by JDBC and a filename
 * to the database. The data source offers connections to the physical database
 * as specified by the file name.
 *
 * The DbContext should be passed around to data access objects (DAO) and other
 * components that need access to the database. In particular, the data source should
 * give access to the database respectively establish connections.
 * For efficiency, a connection pooling can be used.
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

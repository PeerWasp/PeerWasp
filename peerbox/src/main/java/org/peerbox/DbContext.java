package org.peerbox;

import java.nio.file.Path;

import javax.sql.DataSource;

public class DbContext {

	private DataSource dataSource;
	private Path databaseFile;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Path getDatabaseFile() {
		return databaseFile;
	}

	public void setDatabaseFile(Path databaseFile) {
		this.databaseFile = databaseFile;
	}

}

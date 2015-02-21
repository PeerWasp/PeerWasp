
package org.peerbox.misc;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.peerbox.app.DbContext;
import org.peerbox.utils.UserDbUtils;
import org.peerbox.watchservice.filetree.persistency.FileDao;
import org.peerbox.watchservice.filetree.persistency.RemoteFileDao;

public class DbDump {

	private static final Path db = Paths.get("/home/albrecht/PB1/config/688787d8ff144c502c7f5cffaafe2cc588d86079f9de88304c26b0cb99ce91c6.db");

	public static void main(String[] args) {
		DbContext dbContext = UserDbUtils.createDbContext(db.toString());

		FileDao fileDao = new FileDao(dbContext);
		fileDao.dumpCsv();

		RemoteFileDao remoteFileDao = new RemoteFileDao(dbContext);
		remoteFileDao.dumpCsv();
	}

}

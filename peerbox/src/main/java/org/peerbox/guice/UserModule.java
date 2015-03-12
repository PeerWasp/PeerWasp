package org.peerbox.guice;

import java.nio.file.Path;

import org.peerbox.app.DbContext;
import org.peerbox.app.config.UserConfig;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.filerecovery.FileRecoveryUILoader;
import org.peerbox.utils.AppData;
import org.peerbox.utils.UserDbUtils;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.FileTree;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class UserModule extends AbstractModule {

	private final UserConfig userConfig;

	public UserModule(UserConfig userConfig) {
		this.userConfig = userConfig;
	}

	@Override
	protected void configure() {
		bind(UserConfig.class).toInstance(userConfig);

		bind(FileRecoveryUILoader.class);

		bind(IFileManager.class).to(FileManager.class);
		bind(IFileEventManager.class).to(FileEventManager.class);
	}

	@Provides @Singleton
	FileTree provideFileTree(UserConfig cfg){
		return new FileTree(cfg.getRootPath());
	}

	@Provides
	@Singleton
	DbContext provideUserDbContext(UserConfig userConfig) {
		String username = userConfig.getUsername();
		String filename = UserDbUtils.createFileName(username);
		Path dbPath = AppData.getConfigFolder().resolve(filename);

		DbContext dbContext = UserDbUtils.createDbContext(dbPath);
		return dbContext;
	}

}

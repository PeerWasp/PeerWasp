package org.peerbox.guice;

import java.nio.file.Path;

import org.peerbox.IUserConfig;
import org.peerbox.UserConfig;
import org.peerbox.utils.AppData;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class UserConfigModule extends AbstractModule {

	/**
	 * The path to the config file to load.
	 */
	private final Path userConfigFile;

	public UserConfigModule() {
		this(null);
	}

	public UserConfigModule(final Path userConfigFile) {
		if (userConfigFile != null) {
			this.userConfigFile = userConfigFile;
		} else {
			this.userConfigFile = AppData.getConfigFolder().resolve("peerbox.conf");
		}
	}

	@Override
	protected void configure() {
		bind(IUserConfig.class).to(UserConfig.class);
	}

	@Provides
	@Singleton
	private UserConfig provideUserConfig() {

		return new UserConfig(userConfigFile);
	}

}

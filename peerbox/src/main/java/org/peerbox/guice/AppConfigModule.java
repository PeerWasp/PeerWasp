package org.peerbox.guice;

import java.nio.file.Path;

import org.peerbox.app.config.AppConfig;
import org.peerbox.app.config.BootstrappingNodesFactory;
import org.peerbox.utils.AppData;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Module for the application config and "global" settings (i.e. not user specific).
 *
 * @author albrecht
 *
 */
public class AppConfigModule extends AbstractModule {

	/**
	 * The path to the config file to load.
	 */
	private final Path appConfigFile;

	public AppConfigModule() {
		this(null);
	}

	public AppConfigModule(final Path appConfigFile) {
		if (appConfigFile != null) {
			this.appConfigFile = appConfigFile;
		} else {
			this.appConfigFile = AppData.getConfigFolder().resolve("app.conf");
		}
	}

	@Override
	protected void configure() {

	}

	@Provides
	private BootstrappingNodesFactory providesBootstrappingNodesFactory() {
		BootstrappingNodesFactory f = new BootstrappingNodesFactory();

		f.setLastNodeFile(AppData.getConfigFolder().resolve("lastnode"));
		f.setNodesFile(AppData.getConfigFolder().resolve("bootstrappingnodes"));
		f.setNodesDefaultUrl(getClass().getResource("/config/default_bootstrappingnodes"));

		return f;
	}

	@Provides
	@Singleton
	private AppConfig provideAppConfig() {
		AppConfig appConfig = new AppConfig(appConfigFile);
		return appConfig;
	}

}

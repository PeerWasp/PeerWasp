package org.peerbox.app;

import org.peerbox.app.config.UserConfig;
import org.peerbox.guice.UserModule;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ClientContextFactory {

	private Injector injector;

	@Inject
	public ClientContextFactory(Injector injector) {
		this.injector = injector;
	}

	public ClientContext create(UserConfig userConfig) {
		Injector clientInjector = injector.createChildInjector(new UserModule(userConfig));
		ClientContext context = clientInjector.getInstance(ClientContext.class);
		return context;
	}

}

package org.peerbox.app;

import org.peerbox.app.config.UserConfig;
import org.peerbox.guice.UserModule;
import org.peerbox.watchservice.FileEventManager;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ClientContextFactory {

	/* should be the root injector from which child is derived for specific user */
	private final Injector injector;

	@Inject
	public ClientContextFactory(Injector injector) {
		this.injector = injector;
	}

	/**
	 * Creates a new client context given the user information in the user config.
	 *
	 * @param userConfig with user data for which client context should be created
	 * @return client context.
	 */
	public ClientContext create(UserConfig userConfig) {
		// create a child injector. this allows us to have instances and singletons associated with
		// a specific user (logged in user account)
		Injector clientInjector = injector.createChildInjector(new UserModule(userConfig));
		ClientContext context = clientInjector.getInstance(ClientContext.class);

		// register for local/remote events
		FileEventManager fileEventManager = context.getFileEventManager();
		context.getFolderWatchService().addFileEventListener(fileEventManager);
		context.getNodeManager().getNode().getFileManager().subscribeFileEvents(fileEventManager);

		return context;
	}

}


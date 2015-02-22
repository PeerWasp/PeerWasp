package org.peerbox.app;

import org.peerbox.app.config.UserConfig;
import org.peerbox.guice.UserModule;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.filetree.FileTreeInitializer;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ClientContextFactory {

	private Injector injector;

	@Inject
	public ClientContextFactory(Injector injector) {
		this.injector = injector;
	}

	public ClientContext create(UserConfig userConfig) throws Exception {
		// create a child injector. this allows us to have instances and singletons associated with
		// a specific user (logged in user account)
		Injector clientInjector = injector.createChildInjector(new UserModule(userConfig));
		ClientContext context = clientInjector.getInstance(ClientContext.class);

		// register for local/remote events
		FileEventManager fileEventManager = context.getFileEventManager();

		FileTreeInitializer treeInitializer = new FileTreeInitializer(context);
		treeInitializer.initialize();

		context.getFolderWatchService().addFileEventListener(fileEventManager);
		context.getNodeManager().getNode().getFileManager().subscribeFileEvents(fileEventManager);

		return context;
	}

}

package org.peerbox.guice;


import javafx.stage.Stage;

import org.peerbox.app.ExitHandler;
import org.peerbox.app.IExitHandler;
import org.peerbox.app.config.IPeerWaspConfig;
import org.peerbox.app.config.PeerWaspConfig;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.node.NodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.app.manager.user.UserManager;
import org.peerbox.delete.FileDeleteHandler;
import org.peerbox.delete.IFileDeleteHandler;
import org.peerbox.events.MessageBus;
import org.peerbox.filerecovery.FileRecoveryHandler;
import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.forcesync.ForceSyncHandler;
import org.peerbox.forcesync.IForceSyncHandler;
import org.peerbox.share.IShareFolderHandler;
import org.peerbox.share.ShareFolderHandler;
import org.peerbox.view.tray.AbstractSystemTray;
import org.peerbox.view.tray.JSystemTray;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class AppModule extends AbstractModule {

	private final MessageBus messageBus;
	private final Stage primaryStage;

	public AppModule(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.messageBus = new MessageBus();
	}

	@Override
	protected void configure() {

		bindMessageBus();

		bindSystemTray();

		bindPrimaryStage();

		bindManagers();

		bindConfigs();

		bindContextMenuHandlers();

		bind(IFxmlLoaderProvider.class).to(GuiceFxmlLoader.class);
		bind(IExitHandler.class).to(ExitHandler.class);
	}

	private void bindMessageBus() {
		bind(MessageBus.class).toInstance(messageBus);
		messageBusRegisterRule();
	}

	private void messageBusRegisterRule() {
		// This rule registers all objects created by Guice with the message bus.
		// As a result, all instances created by Guice can receive messages without explicit
		// subscription.
		bindListener(Matchers.any(), new TypeListener() {
			@Override
	        public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
	            typeEncounter.register(new InjectionListener<I>() {
	                public void afterInjection(I i) {
	                    messageBus.subscribe(i);
	                }
	            });
	        }
	    });
	}

	private void bindPrimaryStage() {
		bind(javafx.stage.Stage.class)
			.annotatedWith(Names.named("PrimaryStage"))
			.toInstance(primaryStage);
	}

	private void bindSystemTray() {
		bind(AbstractSystemTray.class).to(JSystemTray.class);
	}

	private void bindManagers() {
		bind(INodeManager.class).to(NodeManager.class);
		bind(IUserManager.class).to(UserManager.class);
	}

	private void bindContextMenuHandlers() {
		bind(IFileDeleteHandler.class).to(FileDeleteHandler.class);
		bind(IFileRecoveryHandler.class).to(FileRecoveryHandler.class);
		bind(IShareFolderHandler.class).to(ShareFolderHandler.class);
		bind(IForceSyncHandler.class).to(ForceSyncHandler.class);
	}


	private void bindConfigs() {
		bind(IPeerWaspConfig.class).to(PeerWaspConfig.class);
	}


}

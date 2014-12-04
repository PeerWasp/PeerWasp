package org.peerbox.guice;


import org.peerbox.FileManager;
import org.peerbox.app.ExitHandler;
import org.peerbox.app.IExitHandler;
import org.peerbox.events.MessageBus;
import org.peerbox.filerecovery.FileRecoveryHandler;
import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.model.H2HManager;
import org.peerbox.model.UserManager;
import org.peerbox.share.IShareFolderHandler;
import org.peerbox.share.ShareFolderHandler;
import org.peerbox.view.tray.AbstractSystemTray;
import org.peerbox.view.tray.JSystemTray;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class PeerBoxModule extends AbstractModule {

	private final MessageBus messageBus = new MessageBus();
	
	@Override
	protected void configure() {
		bindMessageBus();
		bindSystemTray();
		bindPrimaryStage();
		
		bind(IFxmlLoaderProvider.class).to(GuiceFxmlLoader.class);
		bind(IFileRecoveryHandler.class).to(FileRecoveryHandler.class);
		bind(IShareFolderHandler.class).to(ShareFolderHandler.class);
		
		bind(IExitHandler.class).to(ExitHandler.class);
	}

	private void bindSystemTray() {
		bind(AbstractSystemTray.class).to(JSystemTray.class);
	}

	private void bindMessageBus() {
		bind(MessageBus.class).toInstance(messageBus);
		messageBusRegisterRule();
	}
	
	private void messageBusRegisterRule() {
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
			.toInstance(org.peerbox.App.getPrimaryStage());
	}

	@Provides
	UserManager providesUserManager(H2HManager manager) {
		return new UserManager(manager.getNode().getUserManager());
	}
	
	@Provides
	FileManager providesFileManager(H2HManager manager) {
		return new FileManager(manager.getNode().getFileManager());
	}

}

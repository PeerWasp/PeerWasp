package org.peerbox.guice;


import org.peerbox.FileManager;
import org.peerbox.interfaces.IFileVersionSelectionUI;
import org.peerbox.model.H2HManager;
import org.peerbox.model.UserManager;
import org.peerbox.view.RecoverFileStage;
import org.peerbox.view.tray.AbstractSystemTray;
import org.peerbox.view.tray.JSystemTray;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class PeerBoxModule extends AbstractModule {

	private final EventBus eventBus = new EventBus("Default EventBus");
	
	@Override
	protected void configure() {
		bindEventBus();
		bindSystemTray();
		bindPrimaryStage();
		
		bind(IFileVersionSelectionUI.class).to(RecoverFileStage.class);
	}

	private void bindSystemTray() {
		bind(AbstractSystemTray.class).to(JSystemTray.class);
	}

	private void bindEventBus() {
		bind(EventBus.class).toInstance(eventBus);
		eventBusRegisterRule();
	}
	
	private void eventBusRegisterRule() {
		bindListener(Matchers.any(), new TypeListener() {
			@Override
	        public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
	            typeEncounter.register(new InjectionListener<I>() {
	                public void afterInjection(I i) {
	                    eventBus.register(i);
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

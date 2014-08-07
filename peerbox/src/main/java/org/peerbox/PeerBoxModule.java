package org.peerbox;


import org.peerbox.model.H2HManager;
import org.peerbox.model.UserManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class PeerBoxModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(javafx.stage.Stage.class).annotatedWith(Names.named("PrimaryStage"))
			.toInstance(org.peerbox.App.getPrimaryStage());
	}
	
	@Provides
	UserManager providesUserManager(H2HManager manager) {
		return new UserManager(manager.getNode().getUserManager());
	}

}

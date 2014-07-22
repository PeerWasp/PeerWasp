package org.peerbox;

import org.peerbox.model.H2HManager;
import org.peerbox.model.UserManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class PeerBoxModule extends AbstractModule {

	@Override
	protected void configure() {

	}
	
	@Provides
	UserManager providesUserManager(H2HManager manager) {
		return new UserManager(manager.getNode());
	}

}

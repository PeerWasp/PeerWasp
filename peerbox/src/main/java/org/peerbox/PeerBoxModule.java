package org.peerbox;

import org.peerbox.model.H2HManager;
import org.peerbox.model.UserManager;
import org.peerbox.presenter.CreateNetworkController;
import org.peerbox.presenter.JoinNetworkController;
import org.peerbox.presenter.LoginController;
import org.peerbox.presenter.NetworkSelectionController;
import org.peerbox.presenter.RegisterController;
import org.peerbox.presenter.SelectRootPathController;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class PeerBoxModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(H2HManager.class);
		bind(CreateNetworkController.class);
		bind(JoinNetworkController.class);
		bind(LoginController.class);
		bind(NetworkSelectionController.class);
		bind(RegisterController.class);
		bind(SelectRootPathController.class);
	}
	
	@Provides
	UserManager providesUserManager(H2HManager manager) {
		return new UserManager(manager.getNode());
	}

}

package org.peerbox;

import org.peerbox.model.H2HManager;
import org.peerbox.presenter.CreateNetworkController;
import org.peerbox.presenter.JoinNetworkController;
import org.peerbox.presenter.LoginController;
import org.peerbox.presenter.NetworkSelectionController;
import org.peerbox.presenter.RegisterController;
import org.peerbox.presenter.SelectRootPathController;

import com.google.inject.AbstractModule;

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

}

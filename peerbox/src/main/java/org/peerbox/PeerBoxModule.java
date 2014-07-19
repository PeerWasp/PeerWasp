package org.peerbox;

import org.peerbox.presenter.NetworkSelectionController;

import com.google.inject.AbstractModule;

public class PeerBoxModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(NetworkSelectionController.class);
	}

}

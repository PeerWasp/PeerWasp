package org.peerbox.server.helper;

import org.peerbox.filerecovery.IFileRecoveryHandler;

import com.google.inject.AbstractModule;

public class ApiServerTestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IFileRecoveryHandler.class).to(RecoverFileMock.class);
	}

}

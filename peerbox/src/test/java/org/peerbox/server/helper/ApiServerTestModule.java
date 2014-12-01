package org.peerbox.server.helper;

import org.peerbox.interfaces.IFileVersionHandler;

import com.google.inject.AbstractModule;

public class ApiServerTestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IFileVersionHandler.class).to(RecoverFileMock.class);
	}

}

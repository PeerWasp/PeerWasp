package org.peerbox.guice.provider;

import org.peerbox.filerecovery.FileRecoveryHandler;
import org.peerbox.filerecovery.IFileRecoveryHandler;

import com.google.inject.Provider;

public final class FileRecoveryHandlerProvider implements Provider<IFileRecoveryHandler> {

	public IFileRecoveryHandler get() {
		return new FileRecoveryHandler();
	}
}

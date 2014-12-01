package org.peerbox.guice;

import org.peerbox.interfaces.IFileVersionHandler;
import org.peerbox.view.RecoverFileStage;

import com.google.inject.Provider;

public class FileVersionSelectionUIProvider implements Provider<IFileVersionHandler> {

	public IFileVersionHandler get() {
		return new RecoverFileStage();
	}
}

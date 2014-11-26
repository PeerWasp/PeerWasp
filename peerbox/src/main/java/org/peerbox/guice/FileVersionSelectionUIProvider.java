package org.peerbox.guice;

import org.peerbox.interfaces.IFileVersionSelectionUI;
import org.peerbox.view.RecoverFileStage;

import com.google.inject.Provider;

public class FileVersionSelectionUIProvider implements Provider<IFileVersionSelectionUI> {

	public IFileVersionSelectionUI get() {
		return new RecoverFileStage();
	}
}

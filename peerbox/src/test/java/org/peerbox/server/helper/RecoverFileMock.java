package org.peerbox.server.helper;

import java.nio.file.Path;

import org.peerbox.interfaces.IFileVersionHandler;

public class RecoverFileMock implements IFileVersionHandler {

	@Override
	public void onFileVersionRequested(Path fileToRecover) {
		System.out.println(fileToRecover.toString());
	}

}

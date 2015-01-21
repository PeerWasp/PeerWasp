package org.peerbox.watchservice;

import java.nio.file.Path;

public interface ILocalFileEventListener {

	void onLocalFileCreated(Path path);

	void onLocalFileDeleted(Path path);

	void onLocalFileModified(Path path);

}

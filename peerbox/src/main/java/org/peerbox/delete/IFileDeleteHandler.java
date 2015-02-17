package org.peerbox.delete;

import java.nio.file.Path;

public interface IFileDeleteHandler {

	void deleteFile(Path fileToDelete);

}

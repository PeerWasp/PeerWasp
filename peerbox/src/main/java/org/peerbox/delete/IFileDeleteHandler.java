package org.peerbox.delete;

import java.nio.file.Path;

/**
 * IFileDeleteHandler defines the interface for deleting files permanently in response
 * to a request from the user.
 *
 * @author albrecht
 *
 */
public interface IFileDeleteHandler {

	/**
	 * Initiates a hard delete which deletes a file permanently.
	 *
	 * @param fileToDelete the file to delete
	 */
	void deleteFile(Path fileToDelete);

}

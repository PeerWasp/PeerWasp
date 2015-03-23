package org.peerbox.filerecovery;

import java.nio.file.Path;

/**
 * IFileRecoveryHandler defines the interface for recovering previous versions of a file in
 * response to a request of the user.
 *
 * @author albrecht
 *
 */
public interface IFileRecoveryHandler {

	/**
	 * Start recovery procedure. The available versions need to be queried first from
	 * which a version can be selected.
	 *
	 * @param fileToRecover
	 */
	void recoverFile(Path fileToRecover);

}

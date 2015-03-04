package org.peerbox.share;

import java.nio.file.Path;

/**
 * Defines the interface for sharing a folder in response to an action of the user
 * (e.g. click on a button or menu).
 * The implementing handlers should take care of the sharing procedure (including loading
 * a GUI to further specify details etc.)
 *
 * @author albrecht
 *
 */
public interface IShareFolderHandler {

	/**
	 * Handles a request to share a folder with other users of the network.
	 *
	 * @param folderToShare path to a folder
	 */
	void shareFolder(Path folderToShare);

}

package org.peerbox.filerecovery;

import java.util.List;

import org.hive2hive.core.model.IFileVersion;

/**
 * Listener interface that receives available versions of a file from the {@link FileVersionSelector}.
 *
 * @author albrecht
 *
 */
public interface IFileVersionSelectorListener {

	void onAvailableVersionsReceived(List<IFileVersion> availableVersions);

}

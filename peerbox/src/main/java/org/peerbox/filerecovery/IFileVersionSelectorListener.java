package org.peerbox.filerecovery;

import java.util.List;

import org.hive2hive.core.model.IFileVersion;


public interface IFileVersionSelectorListener {

	void onAvailableVersionsReceived(List<IFileVersion> availableVersions);

}

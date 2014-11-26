package org.peerbox.interfaces;

import java.util.List;

import org.hive2hive.core.model.IFileVersion;


public interface IFileVersionSelectorEventListener {

	void onAvailableVersionsReceived(List<IFileVersion> availableVersions);
	
}

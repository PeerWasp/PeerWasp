package org.peerbox.watchservice;

import java.util.List;

import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.files.recover.IVersionSelector;

public class PeerboxVersionSelector implements IVersionSelector{

	private int fVersionToRecover;
	
	public PeerboxVersionSelector(int versionToRecover){
		fVersionToRecover = versionToRecover;
	}
	
	@Override
	public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
		// TODO Auto-generated method stub
		for(IFileVersion fileVersion : availableVersions){
			if(fileVersion.getIndex() == fVersionToRecover){
				return fileVersion;
			}
		}
		return null;
	}

	@Override
	public String getRecoveredFileName(String fullName, String name, String extension) {
		//return name + "_v" + fVersionToRecover + "recover" + extension;
		
		// Since the tests need the same path to check for existence, the function is in the static utils.
		return PathUtils.getRecoveredFilePath(fullName, fVersionToRecover).toString();
	}
	
	public int getVersionToRecover(){
		return fVersionToRecover;
	}

}

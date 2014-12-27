package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.peerbox.IUserConfig;

import com.google.inject.Inject;

public class DummyUserConfig implements IUserConfig{

	private static Path rootPath = Paths.get(FileUtils.getUserDirectory().getAbsolutePath(), "PeerBox_Sync_Test");
	
	@Inject
	public DummyUserConfig(){
		
	}
	
	@Override
	public Path getRootPath() {
		return rootPath;
	}

}

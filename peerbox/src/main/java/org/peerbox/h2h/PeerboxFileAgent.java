package org.peerbox.h2h;

import java.io.File;
import java.io.IOException;

import org.hive2hive.core.file.IFileAgent;

public class PeerboxFileAgent implements IFileAgent{

	File fRoot;
	
	public PeerboxFileAgent(File root){
		fRoot = root;
	}
	
	@Override
	public File getRoot() {
		return fRoot;
	}

	@Override
	public void writeCache(String key, byte[] data) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] readCache(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}

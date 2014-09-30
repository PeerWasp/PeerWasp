package org.peerbox.watchservice;

public abstract class AbstractFileComponent implements FileComponent {
	
	@Override
	public boolean isFolder() {
		return !isFile();
	}
	
}

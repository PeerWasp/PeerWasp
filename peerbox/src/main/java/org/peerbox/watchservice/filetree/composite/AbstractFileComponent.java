package org.peerbox.watchservice.filetree.composite;


public abstract class AbstractFileComponent implements FileComponent {
	
	@Override
	public boolean isFolder() {
		return !isFile();
	}
	
}

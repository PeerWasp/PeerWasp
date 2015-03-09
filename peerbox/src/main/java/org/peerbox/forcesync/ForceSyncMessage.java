package org.peerbox.forcesync;

import java.nio.file.Path;

import org.peerbox.events.IMessage;

public class ForceSyncMessage implements IMessage {

	Path topLevel;
	
	public ForceSyncMessage(Path topLevel){
		this.topLevel = topLevel;
	}
	
	public Path getTopLevel(){
		return topLevel;
	}
}

package org.peerbox.notifications;

import org.peerbox.events.IMessage;


public class AggregatedFileEventStatus implements IMessage {
	
	private int numFilesAdded;
	private int numFilesModified;
	private int numFilesDeleted;
	private int numFilesMoved;
	
	public AggregatedFileEventStatus(int numAdded, int numModified, int numDeleted, int numMoved) {
		numFilesAdded = numAdded;
		numFilesModified = numModified;
		numFilesDeleted = numDeleted;
		numFilesMoved = numMoved;
	}

	public int getNumFilesAdded() {
		return numFilesAdded;
	}

	public int getNumFilesModified() {
		return numFilesModified;
	}

	public int getNumFilesDeleted() {
		return numFilesDeleted;
	}
	
	public int getNumFilesMoved(){
		return numFilesMoved;
	}

}

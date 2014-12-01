package org.peerbox.notifications;

import org.peerbox.events.IMessage;


public class AggregatedFileEventStatus implements IMessage {
	
	private int numFilesAdded;
	private int numFilesModified;
	private int numFilesDeleted;
	
	public AggregatedFileEventStatus(int numAdded, int numModified, int numDeleted) {
		numFilesAdded = numAdded;
		numFilesModified = numModified;
		numFilesDeleted = numDeleted;
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

}

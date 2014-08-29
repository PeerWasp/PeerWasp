package org.peerbox.watchservice;

import java.util.Calendar;

public class FileAction {
	
	private long timestamp = Long.MAX_VALUE;
	
	public FileAction() {
		timestamp = Calendar.getInstance().getTimeInMillis();
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
}

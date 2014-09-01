package org.peerbox.watchservice;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileAction {
	
	private final static Logger logger = LoggerFactory.getLogger(FileAction.class);
	private long timestamp = Long.MAX_VALUE;
	
	public FileAction() {
		timestamp = Calendar.getInstance().getTimeInMillis();
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void execute() {
		logger.debug("Execute action...");
		// this may be async, i.e. do not wait on completion of the process
		// maybe return the IProcessComponent object such that the 
		// executor can be aware of the status (completion of task etc)
	}

	public void setTimeStamp(long timestamp) {
		// TODO Auto-generated method stub
		if(timestamp < this.timestamp){
			//this is clearly an error - but can it even occur?
		}
		this.timestamp = timestamp;
	}
	
}

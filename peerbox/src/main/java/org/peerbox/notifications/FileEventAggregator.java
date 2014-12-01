package org.peerbox.notifications;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.peerbox.events.MessageBus;


public class FileEventAggregator {
	
	protected static final int AGGREGATION_TIMESPAN = 10000;
	
	private MessageBus messageBus;
	
	private List<Path> addedFiles;
	private List<Path> modifiedFiles;
	private List<Path> deletedFiles;
	
	private Timer timer;
	
	public FileEventAggregator(MessageBus messageBus) {
		this.messageBus = messageBus;
		addedFiles = new ArrayList<Path>();
		modifiedFiles = new ArrayList<Path>();
		deletedFiles = new ArrayList<Path>();
	}
	
	public void onFileAdded(Path path) {
		addedFiles.add(path);
		scheduleNotification();
	}

	public void onFileModified(Path path) {
		modifiedFiles.add(path);
		scheduleNotification();
	}
	
	public void onFileDeleted(Path path) {
		deletedFiles.add(path);
		scheduleNotification();
	}
	
	private void scheduleNotification() {
		if(timer == null) {
			timer = new Timer(getClass().getName());
			timer.schedule(new TimerTask() {
				
				List<Path> added = null;
				List<Path> modified = null;
				List<Path> deleted = null;
				
				@Override
				public void run() {
					timer = null;
					
					synchronized (addedFiles) {
						added = addedFiles;
						addedFiles = new ArrayList<Path>();
					}
					synchronized (modifiedFiles) {
						modified = modifiedFiles;
						modifiedFiles = new ArrayList<Path>();
					}
					synchronized (deletedFiles) {
						deleted = deletedFiles;
						deletedFiles = new ArrayList<Path>();
					}
					  
					StringBuilder sb = new StringBuilder();
					sb.append("Added Files: " + added.size()).append("\n");
					sb.append("Modified Files: " + modified.size()).append("\n");
					sb.append("Deleted Files: " + deleted.size()).append("\n");
					AggregatedFileEventStatus event = new AggregatedFileEventStatus(added.size(),
							modified.size(), deleted.size());
					
					messageBus.post(event).now();
					  
				}
			}, AGGREGATION_TIMESPAN);
		}
		// else: already scheduled... 
	}
	
}

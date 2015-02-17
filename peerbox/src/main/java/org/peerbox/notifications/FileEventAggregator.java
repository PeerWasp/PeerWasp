package org.peerbox.notifications;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.manager.file.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.RemoteFileAddedMessage;
import org.peerbox.app.manager.file.RemoteFileUpdatedMessage;
import org.peerbox.events.IMessageListener;
import org.peerbox.events.MessageBus;
import org.peerbox.watchservice.FileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileEventAggregator implements IMessageListener{
	
	private static final Logger logger = LoggerFactory.getLogger(FileEventAggregator.class);
	protected static final int AGGREGATION_TIMESPAN = 10000;
	
	private MessageBus messageBus;
	
	private List<Path> addedFiles;
	private List<Path> movedFiles;
	private List<Path> updatedFiles;
	private List<Path> deletedFiles;
	
	private Timer timer;
	
	public FileEventAggregator(MessageBus messageBus) {
		this.messageBus = messageBus;
		addedFiles = new ArrayList<Path>();
		movedFiles = new ArrayList<Path>();
		updatedFiles = new ArrayList<Path>();
		deletedFiles = new ArrayList<Path>();
	}
	
	@Handler
	public void onFileAdded(RemoteFileAddedMessage message) {
		logger.trace("onFileAdded.");
		Path path = message.getPath();
		addedFiles.add(path);
		scheduleNotification();
	}
	
	@Handler
	public void onFileMoved(RemoteFileMovedMessage message){
		logger.trace("onFileMoved.");
		Path path = message.getPath();
		movedFiles.add(path);
		scheduleNotification();
	}

	@Handler
	public void onFileUpdated(RemoteFileUpdatedMessage message) {
		logger.trace("onFileUpdated.");
		Path path = message.getPath();
		updatedFiles.add(path);
		scheduleNotification();
	}
	
	@Handler
	public void onFileDeleted(RemoteFileDeletedMessage message) {
		logger.trace("onFileDeleted.");
		Path path = message.getPath();
		deletedFiles.add(path);
		scheduleNotification();
	}
	
	private void scheduleNotification() {
		if(timer == null) {
			timer = new Timer(getClass().getName());
			timer.schedule(new TimerTask() {
				
				List<Path> added = null;
				List<Path> updated = null;
				List<Path> deleted = null;
				List<Path> moved = null;
				
				@Override
				public void run() {
					timer = null;
					
					synchronized (addedFiles) {
						added = addedFiles;
						addedFiles = new ArrayList<Path>();
					}
					synchronized (updatedFiles) {
						updated = updatedFiles;
						updatedFiles = new ArrayList<Path>();
					}
					synchronized (deletedFiles) {
						deleted = deletedFiles;
						deletedFiles = new ArrayList<Path>();
					}
					synchronized (movedFiles){
						moved = movedFiles;
						movedFiles = new ArrayList<Path>();
					}
					
					AggregatedFileEventStatus event = new AggregatedFileEventStatus(added.size(),
							updated.size(), deleted.size(), moved.size());
					
					messageBus.post(event).now();
					messageBus.publish(event);
					  
				}
			}, AGGREGATION_TIMESPAN);
		}
		// else: already scheduled... 
	}
	
}

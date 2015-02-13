package org.peerbox.notifications;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.manager.file.FileDeleteMessage;
import org.peerbox.app.manager.file.FileDownloadMessage;
import org.peerbox.app.manager.file.FileUploadMessage;
import org.peerbox.events.IMessageListener;
import org.peerbox.events.MessageBus;


public class FileEventAggregator implements IMessageListener{
	
	protected static final int AGGREGATION_TIMESPAN = 10000;
	
	private MessageBus messageBus;
	
	private List<Path> uploadedFiles;
	private List<Path> downloadedFiles;
	private List<Path> modifiedFiles;
	private List<Path> deletedFiles;
	
	private Timer timer;
	
	public FileEventAggregator(MessageBus messageBus) {
		this.messageBus = messageBus;
		uploadedFiles = new ArrayList<Path>();
		downloadedFiles = new ArrayList<Path>();
		modifiedFiles = new ArrayList<Path>();
		deletedFiles = new ArrayList<Path>();
	}
	
	@Handler
	public void onFileUploaded(FileUploadMessage message) {
		Path path = message.getPath();
		uploadedFiles.add(path);
		scheduleNotification();
	}
	
	@Handler
	public void onFileDownloaded(FileDownloadMessage message){
		Path path = message.getPath();
		downloadedFiles.add(path);
		scheduleNotification();
	}

	public void onFileModified(Path path) {
		modifiedFiles.add(path);
		scheduleNotification();
	}
	
	@Handler
	public void onFileDeleted(FileDeleteMessage message) {
		Path path = message.getPath();
		deletedFiles.add(path);
		scheduleNotification();
	}
	
	private void scheduleNotification() {
		if(timer == null) {
			timer = new Timer(getClass().getName());
			timer.schedule(new TimerTask() {
				
				List<Path> uploaded = null;
				List<Path> modified = null;
				List<Path> deleted = null;
				List<Path> downloaded = null;
				
				@Override
				public void run() {
					timer = null;
					
					synchronized (uploadedFiles) {
						uploaded = uploadedFiles;
						uploadedFiles = new ArrayList<Path>();
					}
					synchronized (modifiedFiles) {
						modified = modifiedFiles;
						modifiedFiles = new ArrayList<Path>();
					}
					synchronized (deletedFiles) {
						deleted = deletedFiles;
						deletedFiles = new ArrayList<Path>();
					}
					synchronized (downloadedFiles){
						downloaded = downloadedFiles;
						downloadedFiles = new ArrayList<Path>();
					}
					  
					StringBuilder sb = new StringBuilder();
					sb.append("Uploaded Files: " + uploaded.size()).append("\n");
					sb.append("Downloaded Files: " + downloaded.size()).append("\n");
					sb.append("Deleted Files: " + deleted.size()).append("\n");
					AggregatedFileEventStatus event = new AggregatedFileEventStatus(uploaded.size(),
							modified.size(), deleted.size());
					
					messageBus.post(event).now();
					messageBus.publish(event);
					  
				}
			}, AGGREGATION_TIMESPAN);
		}
		// else: already scheduled... 
	}
	
}

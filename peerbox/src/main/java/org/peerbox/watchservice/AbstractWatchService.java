package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWatchService {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractWatchService.class);
	
	private final List<IFileEventListener> eventListeners;
	private final BlockingQueue<INotifyFileEvent> eventQueue;
	private Thread notifyThread;

	public AbstractWatchService() {
		super();
		this.eventListeners = new ArrayList<IFileEventListener>();
		this.eventQueue = new LinkedBlockingQueue<INotifyFileEvent>();
	}

	public void start() throws Exception {
		eventQueue.clear();
		notifyThread = new Thread(new NotifyEventListeners());
		notifyThread.start();
		logger.info("Watch Service started.");
	}

	public void stop() throws Exception {
		if(notifyThread != null) {
			notifyThread.interrupt();
			notifyThread.join();
			notifyThread = null;
		}
		eventQueue.clear();
		logger.info("Watch Service stopped.");
	}

	public synchronized void addFileEventListener(final IFileEventListener listener) {
		eventListeners.add(listener);
	}

	public synchronized void removeFileEventListener(final IFileEventListener listener) {
		eventListeners.remove(listener);
	}

	private void notifyFileCreated(final Path path) {
		List<IFileEventListener> listeners = new ArrayList<IFileEventListener>(eventListeners);
		for(IFileEventListener l : listeners) {
			l.onFileCreated(path);
		}
	}

	private void notifyFileModified(final Path path) {
		List<IFileEventListener> listeners = new ArrayList<IFileEventListener>(eventListeners);
		for(IFileEventListener l : listeners) {
			l.onFileModified(path);
		}
	}

	private void notifyFileDeleted(final Path path) {
		List<IFileEventListener> listeners = new ArrayList<IFileEventListener>(eventListeners);
		for(IFileEventListener l : listeners) {
			l.onFileDeleted(path);
		}
	}
	
	protected void addNotifyEvent(INotifyFileEvent event) throws InterruptedException {
		eventQueue.put(event);
	}
	
	private class NotifyEventListeners implements Runnable {
		@Override
		public void run() {
			processEventQueue();
		}

		private void processEventQueue() {
			while(true) {
				try {
					INotifyFileEvent event = eventQueue.take();
					event.notifyEventListeners();
				} catch (InterruptedException iex) {
//					logger.debug("Processing event queue interrupted (stop notifying listeners).");
					return;
				}
			}
		}
	}
	
	protected interface INotifyFileEvent {
		public void notifyEventListeners();
	}
	
	protected class NotifyFileCreated implements INotifyFileEvent {
		private final Path path;
		public NotifyFileCreated(Path path) {
			this.path = path;
		}
		@Override
		public void notifyEventListeners() {
			notifyFileCreated(path);
		}
	}
	
	protected class NotifyFileModified implements INotifyFileEvent {
		private final Path path;
		public NotifyFileModified(Path path) {
			this.path = path;
		}
		@Override
		public void notifyEventListeners() {
			notifyFileModified(path);
		}
	}
	
	protected class NotifyFileDeleted implements INotifyFileEvent {
		private final Path path;
		public NotifyFileDeleted(Path path) {
			this.path = path;
		}
		@Override
		public void notifyEventListeners() {
			notifyFileDeleted(path);
		}
	}
}
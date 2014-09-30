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
	
	private final List<ILocalFileEventListener> eventListeners;
	private final BlockingQueue<INotifyFileEvent> eventQueue;
	private Thread notifyThread;

	public AbstractWatchService() {
		super();
		this.eventListeners = new ArrayList<ILocalFileEventListener>();
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

	public synchronized void addFileEventListener(final ILocalFileEventListener listener) {
		eventListeners.add(listener);
	}

	public synchronized void removeFileEventListener(final ILocalFileEventListener listener) {
		eventListeners.remove(listener);
	}

	private void notifyFileCreated(final Path path) {
		List<ILocalFileEventListener> listeners = new ArrayList<ILocalFileEventListener>(eventListeners);
		for(ILocalFileEventListener l : listeners) {
			l.onLocalFileCreated(path, true);
		}
	}

	private void notifyFileModified(final Path path) {
		List<ILocalFileEventListener> listeners = new ArrayList<ILocalFileEventListener>(eventListeners);
		for(ILocalFileEventListener l : listeners) {
			l.onLocalFileModified(path);
		}
	}

	private void notifyFileDeleted(final Path path) {
		List<ILocalFileEventListener> listeners = new ArrayList<ILocalFileEventListener>(eventListeners);
		for(ILocalFileEventListener l : listeners) {
			l.onLocalFileDeleted(path);
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
		public void logEvent();
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
		@Override
		public void logEvent() {
			logger.debug("Notify CREATED - {}", path);
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
		@Override
		public void logEvent() {
			logger.debug("Notify MODIFIED - {}", path);
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
		@Override
		public void logEvent() {
			logger.debug("Notify DELETED - {}", path);
		}
	}
}
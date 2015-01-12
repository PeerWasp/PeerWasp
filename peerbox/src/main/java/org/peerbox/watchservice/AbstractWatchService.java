package org.peerbox.watchservice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWatchService {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractWatchService.class);
	
	private final List<ILocalFileEventListener> eventListeners;
	private final BlockingQueue<INotifyFileEvent> eventQueue;
	private Thread notifierThread;
	
	private Path folderToWatch;
	
	protected final AtomicBoolean isRunning;

	public AbstractWatchService() {
		super();
		isRunning = new AtomicBoolean(false);
		this.eventListeners = new CopyOnWriteArrayList<ILocalFileEventListener>();
		this.eventQueue = new LinkedBlockingQueue<INotifyFileEvent>();
	}

	public final void start(final Path folderToWatch) throws Exception {
		if (folderToWatch == null) {
			throw new IllegalArgumentException("Parameter folderToWatch must not be null.");
		}
		if (!Files.exists(folderToWatch)) {
			throw new IllegalArgumentException("The folder folderToWatch does not exist.");
		}
		if (!Files.isDirectory(folderToWatch)) {
			throw new IllegalArgumentException("Parameter folderToWatch must not point to a file.");
		}
		
		this.folderToWatch = folderToWatch;
		eventQueue.clear();		
		notifierThread = new Thread(new EventListenerNotifier());
		notifierThread.setName("WatchServiceNotifier");
		notifierThread.start();
		
		onStarted();
		
		isRunning.set(true);
	}

	protected abstract void onStarted() throws Exception;
	
	public final void stop() throws Exception {
		isRunning.set(false);
		
		if (notifierThread != null) {
			notifierThread.interrupt();
			notifierThread = null;
		}
		eventQueue.clear();
		
		onStopped();
	}
	
	protected abstract void onStopped() throws Exception;

	public synchronized void addFileEventListener(final ILocalFileEventListener listener) {
		eventListeners.add(listener);
	}

	public synchronized void removeFileEventListener(final ILocalFileEventListener listener) {
		eventListeners.remove(listener);
	}
	
	protected Path getFolderToWatch() {
		return folderToWatch;
	}
	
	private void notifyFileCreated(final Path path) {
		for (ILocalFileEventListener l : eventListeners) {
			l.onLocalFileCreated(path);
		}
	}

	private void notifyFileModified(final Path path) {
		for (ILocalFileEventListener l : eventListeners) {
			l.onLocalFileModified(path);
		}
	}

	private void notifyFileDeleted(final Path path) {
		for (ILocalFileEventListener l : eventListeners) {
			l.onLocalFileDeleted(path);
		}
	}
	
	protected void addNotifyEvent(final INotifyFileEvent event) throws InterruptedException {
		eventQueue.put(event);
	}
	
	private class EventListenerNotifier implements Runnable {
		@Override
		public void run() {
			processEventQueue();
			logger.info("Notifier thread exiting.");
		}

		private void processEventQueue() {
			while (true) {
				try {
					
					INotifyFileEvent event = eventQueue.take();
					event.notifyEventListeners();
				} catch (InterruptedException iex) {
					if (isRunning.get()) {
						// stop not called - unexpected!
						logger.warn("Notifier thread interrupted unexpectedly. Stop sending notifications to event listeners.");
					} else {
						// if stop() called, notifier thread gets interrupted
						logger.trace("Stop processing event queue (stop sending notifications to event listeners).");
					}
					return;
				} catch (Exception ex) {
					logger.warn("Exception catched: {}", ex.getMessage(), ex);
				}
			}
		}
	}
	
	protected interface INotifyFileEvent {
		void notifyEventListeners();
		void logEvent();
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
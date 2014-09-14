package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWatchService {

	protected List<IFileEventListener> eventListeners;

	public AbstractWatchService() {
		super();
		this.eventListeners = new ArrayList<IFileEventListener>();
	}

	public synchronized void addFileEventListener(IFileEventListener listener) {
		eventListeners.add(listener);
	}

	public synchronized void removeFileEventListener(IFileEventListener listener) {
		eventListeners.remove(listener);
	}

	protected void notifyFileCreated(Path path) {
		List<IFileEventListener> listeners = new ArrayList<IFileEventListener>(eventListeners);
		for(IFileEventListener l : listeners) {
			l.onFileCreated(path);
		}
	}

	protected void notifyFileModified(Path path) {
		List<IFileEventListener> listeners = new ArrayList<IFileEventListener>(eventListeners);
		for(IFileEventListener l : listeners) {
			l.onFileModified(path);
		}
	}

	protected void notifyFileDeleted(Path path) {
		List<IFileEventListener> listeners = new ArrayList<IFileEventListener>(eventListeners);
		for(IFileEventListener l : listeners) {
			l.onFileDeleted(path);
		}
	}

	public abstract void start() throws Exception;
	public abstract void stop() throws Exception;
}
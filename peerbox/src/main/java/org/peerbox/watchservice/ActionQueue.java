package org.peerbox.watchservice;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.peerbox.watchservice.filetree.composite.FileComponent;

public class ActionQueue {

	private static final int QUEUE_CAPACITY = 100;
	private final BlockingQueue<FileComponent> queue;

	public ActionQueue() {
		queue = new PriorityBlockingQueue<>(QUEUE_CAPACITY, new FileActionTimeComparator());
	}

	public FileComponent take() throws InterruptedException {
		return queue.take();
	}

	public void add(FileComponent element) {
		queue.add(element);
	}

	public void remove(FileComponent element) {
		queue.remove(element);
	}

	public Iterator<FileComponent> iterator() {
		return queue.iterator();
	}

	public int size() {
		return queue.size();
	}

	public BlockingQueue<FileComponent> getQueue() {
		return queue;
	}

	private class FileActionTimeComparator implements Comparator<FileComponent> {
		@Override
		public int compare(FileComponent a, FileComponent b) {
			return Long.compare(a.getAction().getTimestamp(), b.getAction().getTimestamp());
		}
	}

}

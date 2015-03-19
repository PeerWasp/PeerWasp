package org.peerbox.watchservice;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.peerbox.watchservice.filetree.composite.FileComponent;

/**
 * This class is a wrapper for a {@link java.util.concurrent.BlockingQueue
 * } storing objects of type {@link org.peerbox.watchservice.filetree.composite.FileComponent
 * FileComponent}. This class is used to build to queue of pending Actions.

 * @author Andreas
 *
 */
public class FileComponentQueue {

	private static final int QUEUE_CAPACITY = 100;
	private final BlockingQueue<FileComponent> queue;

	public FileComponentQueue() {
		queue = new PriorityBlockingQueue<>(QUEUE_CAPACITY, new FileActionTimeComparator());
	}

	public FileComponent take() throws InterruptedException {
		return queue.take();
	}

	public void add(FileComponent element) {
		queue.add(element);
	}

	public boolean remove(FileComponent element) {
		return queue.remove(element);
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

	public void clear() {
		queue.clear();
	}

}

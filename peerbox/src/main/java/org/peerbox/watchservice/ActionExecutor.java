package org.peerbox.watchservice;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.states.LocalDeleteState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;

/**
 * The FileActionExecutor service observes a set of file actions in a queue.
 * An action is executed as soon as it is considered to be "stable", i.e. no more events were 
 * captured within a certain period of time.
 * 
 * @author albrecht
 *
 */
public class ActionExecutor implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
	
	/**
	 *  amount of time that an action has to be "stable" in order to be executed 
	 */
	public static final int ACTION_WAIT_TIME_MS = 2000;
	
	private FileEventManager fileEventManager;

	public ActionExecutor(FileEventManager eventManager) {
		this.fileEventManager = eventManager;
	}
	

	@Override
	public void run() {
		try {
			processActions();
		} catch (NoSessionException | NoPeerConnectionException | IllegalFileLocation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Processes the action in the action queue, one by one.
	 * @throws IllegalFileLocation 
	 * @throws NoPeerConnectionException 
	 * @throws NoSessionException 
	 */
	private synchronized void processActions() throws NoSessionException, NoPeerConnectionException, IllegalFileLocation {
		while(true) {
			FileComponent next = null;
			try {
				// blocking, waits until queue not empty, returns and removes (!) first element
				// synchronized such that no access to the queue is possible between take()/put() call pairs.
				synchronized(this){
					next = fileEventManager.getFileComponentQueue().take();
					if(isActionReady(next.getAction())) {
						
						if(next.getAction().getCurrentState() instanceof LocalDeleteState){
							removeFromDeleted(next);
						}
						next.getAction().execute(fileEventManager.getFileManager());
						next.setIsUploaded(true);
					} else {
						// not ready yet, insert action again (no blocking peek, unfortunately)
						fileEventManager.getFileComponentQueue().put(next);
						long timeToWait = ACTION_WAIT_TIME_MS - getActionAge(next.getAction()) + 1;
						// TODO: does this work? sleep is not so good because it blocks everything...
						if(timeToWait > 0){
							wait(timeToWait);				
						}

					}
				}
				next = fileEventManager.getFileComponentQueue().take();
				fileEventManager.getFileComponentQueue().put(next);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}


	private void removeFromDeleted(FileComponent next) {
		Set<FileComponent> sameHashDeletes = fileEventManager.getDeletedFileComponents().get(next.getContentHash());
		Iterator<FileComponent> componentIterator = sameHashDeletes.iterator();
		while(componentIterator.hasNext()){
			FileComponent candidate = componentIterator.next();
			if(candidate.getPath().toString().equals(next.getPath().toString())){
				componentIterator.remove();
				break;
			}
		}
		Map<String, FolderComposite> deletedByContentNamesHash = fileEventManager.getDeletedByContentNamesHash();
		if(next instanceof FolderComposite){
			FolderComposite nextAsFolder = (FolderComposite)next;
			deletedByContentNamesHash.remove(nextAsFolder.getContentNamesHash());
		}

	}
	
	/**
	 * Checks whether an action is ready to be executed
	 * @param action Action to be executed
	 * @return true if ready to be executed, false otherwise
	 */
	private boolean isActionReady(Action action) {
		long ageMs = getActionAge(action);
		return ageMs >= ACTION_WAIT_TIME_MS;
	}
	
	/**
	 * Computes the age of an action
	 * @param action
	 * @return age in ms
	 */
	private long getActionAge(Action action) {
		return System.currentTimeMillis() - action.getTimestamp();
	}
	
}

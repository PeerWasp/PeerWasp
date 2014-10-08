package org.peerbox.watchservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.watchservice.states.LocalDeleteState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The FileActionExecutor service observes a set of file actions in a queue.
 * An action is executed as soon as it is considered to be "stable", i.e. no more events were 
 * captured within a certain period of time.
 * 
 * @author albrecht
 *
 */
public class ActionExecutor implements Runnable, IActionEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
	
	/**
	 *  amount of time that an action has to be "stable" in order to be executed 
	 */
	public static final long ACTION_WAIT_TIME_MS = 2000;
	public static final int NUMBER_OF_EXECUTE_SLOTS = 4;
	
	private FileEventManager fileEventManager;
	private List<Action> executingActions;

	public ActionExecutor(FileEventManager eventManager) {
		this.fileEventManager = eventManager;
		executingActions = Collections.synchronizedList(new ArrayList<Action>());
	}
	

	@Override
	public void run() {
		processActions();
	}

	/**
	 * Processes the action in the action queue, one by one.
	 * @throws IllegalFileLocation 
	 * @throws NoPeerConnectionException 
	 * @throws NoSessionException 
	 */
	private synchronized void processActions() {
		while (true) {
			try {
				FileComponent next = null;

				// blocking, waits until queue not empty, returns and removes (!) first element
				// synchronized such that no access to the queue is possible between take()/put() call pairs.
//				synchronized (this) {
					logger.debug("Currently executing/pending actions: {}/{}", executingActions.size(), fileEventManager.getFileComponentQueue().size());
					next = fileEventManager.getFileComponentQueue().take();
					if (isActionReady(next.getAction()) && isExecuteSlotFree()) {

						if (next.getAction().getCurrentState() instanceof LocalDeleteState) {
							removeFromDeleted(next);
						}
						// execute
						next.getAction().addEventListener(this);
						executingActions.add(next.getAction());
						next.getAction().execute(fileEventManager.getFileManager());
						next.setIsUploaded(true);
					} else {
						if(executingActions.size() != 0)
							System.out.println("Blocking action: " + executingActions.get(0).getFilePath() + " " + executingActions.get(0).getCurrentState().getClass());
						
						//System.out.println("Current state: " + next.getAction().getCurrentState().getClass().toString());
						// not ready yet, insert action again (no blocking peek, unfortunately)
						fileEventManager.getFileComponentQueue().put(next);
						long timeToWait = calculateWaitTime(next);
						// TODO: does this work? sleep is not so good because it blocks everything...
						wait(timeToWait);
					}
//				}
				next = fileEventManager.getFileComponentQueue().take();
				fileEventManager.getFileComponentQueue().put(next);

				
			} catch (InterruptedException iex) {
				iex.printStackTrace();
				return;
			} catch (Exception ex) {
				logger.warn("Exception occurred: {}", ex.getMessage());
			}
		}
	}


	private boolean isExecuteSlotFree() {
		return executingActions.size() < NUMBER_OF_EXECUTE_SLOTS;
	}


	private long calculateWaitTime(FileComponent action) {
		long timeToWait = ACTION_WAIT_TIME_MS - getActionAge(action.getAction()) + 1L;
		if(timeToWait < 500L) { // wait at least some time
			timeToWait = 500L;
		}
		return timeToWait;
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


	@Override
	public void onActionExecuteSucceeded(Action action) {
		executingActions.remove(action);
		logger.debug("Action successful: {} {}", action.getFilePath(), action.getCurrentState().getClass().toString());
		//logger.debug("Currently executing/pending actions: {}/{}", executingActions.size(), fileEventManager.getFileComponentQueue().size());
	}


	@Override
	public void onActionExecuteFailed(Action action) {
		executingActions.remove(action);
		logger.debug("Action failed: {} {}", action.getFilePath(), action.getCurrentState().getClass().toString());
		//logger.debug("Currently executing/pending actions: {}/{}", executingActions.size(), fileEventManager.getFileComponentQueue().size());
	}
	
}

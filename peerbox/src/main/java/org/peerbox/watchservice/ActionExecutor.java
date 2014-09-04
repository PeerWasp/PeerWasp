package org.peerbox.watchservice;

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;


/**
 * The FileActionExecutor service observes a set of file actions in a queue.
 * An action is executed as soon as it is considered to be "stable", i.e. no more events were 
 * captured within a certain period of time.
 * 
 * @author albrecht
 *
 */
public class ActionExecutor implements Runnable {
	
	/**
	 *  amount of time that an action has to be "stable" in order to be executed 
	 */
	public static final int ACTION_WAIT_TIME_MS = 3000;
	
	private BlockingQueue<Action> actionQueue;
	private BlockingQueue<Action> deleteQueue;
	private Calendar calendar;

	public ActionExecutor(BlockingQueue<Action> actionQueue, BlockingQueue<Action> deleteQueue) {
		this.actionQueue = actionQueue;
		this.deleteQueue = deleteQueue;
		this.calendar = Calendar.getInstance();
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
			Action next = null;
			try {
				System.out.println("1. actionQueue.size: " + actionQueue.size() + " deleteQueue.size(): " + deleteQueue.size());
				// blocking, waits until queue not empty, returns and removes (!) first element
				next = actionQueue.take();
				if(next.getCurrentState() instanceof DeleteState){
					deleteQueue.remove(next);
				}
				
				if(isActionReady(next)) {
					System.out.println("Execute Action");
					next.execute();
				} else {
					// not ready yet, insert action again (no blocking peek, unfortunately)
					actionQueue.put(next);
					if(next.getCurrentState() instanceof DeleteState){
						deleteQueue.add(next);
					}
					long timeToWait =ACTION_WAIT_TIME_MS - getActionAge(next) + 1;
					// TODO: does this work? sleep is not so good because it blocks everything...
					wait(timeToWait);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
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
		return calendar.getTimeInMillis() - action.getTimestamp();
	}
	
}

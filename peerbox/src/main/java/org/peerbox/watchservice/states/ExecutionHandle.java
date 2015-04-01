package org.peerbox.watchservice.states;

import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.watchservice.IAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulation class for the ProcessHandles in Hive2Hive.
 * @author Claudio
 *
 */
public class ExecutionHandle {

	private final static Logger logger = LoggerFactory.getLogger(ExecutionHandle.class);
	private IAction action;
	private ProcessHandle<Void> processHandle;
	private int timeouts = 0;

	public ExecutionHandle(IAction action, ProcessHandle<Void> processHandle) {
		this.action = action;
		this.processHandle = processHandle;
	}

	public IAction getAction() {
		return action;
	}

	public ProcessHandle<Void> getProcessHandle() {
		return processHandle;
	}

	public void incrementTimeouts() {
		timeouts++;
		logger.trace("Incremented timeout to {}", timeouts);
	}

	public int getTimeouts() {
		return timeouts;
	}

	public void setTimeouts(int i) {
		logger.trace("set timeout to {}", i);
		timeouts = i;
	}
}

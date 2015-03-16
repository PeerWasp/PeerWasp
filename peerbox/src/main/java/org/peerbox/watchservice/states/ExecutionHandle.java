package org.peerbox.watchservice.states;

import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.watchservice.IAction;

public class ExecutionHandle {

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
	
	public void incrementTimeouts(){
		timeouts++;
	}
	
	public int getTimeouts(){
		return timeouts;
	}
}

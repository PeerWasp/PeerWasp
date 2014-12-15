package org.peerbox.watchservice;

import org.peerbox.h2h.ProcessHandle;


public interface IActionEventListener {
	void onActionExecuteSucceeded(IAction action);
	void onActionExecuteFailed(IAction action, ProcessHandle<Void> asyncHandle);
}

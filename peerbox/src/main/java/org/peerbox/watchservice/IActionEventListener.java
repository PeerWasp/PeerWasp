package org.peerbox.watchservice;

public interface IActionEventListener {
	void onActionExecuteSucceeded(Action action);
	void onActionExecuteFailed(Action action);
}

package org.peerbox.watchservice;

import org.hive2hive.processframework.RollbackReason;

public interface IActionEventListener {
	void onActionExecuteSucceeded(IAction action);
	void onActionExecuteFailed(IAction action, RollbackReason reason);
}

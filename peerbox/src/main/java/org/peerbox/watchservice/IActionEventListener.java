package org.peerbox.watchservice;

import org.hive2hive.processframework.RollbackReason;

public interface IActionEventListener {
	void onActionExecuteSucceeded(Action action);
	void onActionExecuteFailed(Action action, RollbackReason reason);
}

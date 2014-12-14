package org.peerbox.watchservice;

import org.hive2hive.processframework.exceptions.ProcessExecutionException;


public interface IActionEventListener {
	void onActionExecuteSucceeded(IAction action);
	void onActionExecuteFailed(IAction action, ProcessExecutionException pex);
}

package org.peerbox.h2h;

import java.util.concurrent.Future;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

public class AsyncHandle<T> {
	private IProcessComponent<T> processHandle;
	private Future<T> futureHandle;
	
	public AsyncHandle(IProcessComponent<T> processHandle) {
		this.processHandle = processHandle;
	}
	
	public IProcessComponent<T> getProcess() {
		return processHandle;
	}
	
	public Future<T> getFuture() {
		return futureHandle;
	}
	
	public void start() throws InvalidProcessStateException, ProcessExecutionException {
		futureHandle = processHandle.executeAsync();
	}
}

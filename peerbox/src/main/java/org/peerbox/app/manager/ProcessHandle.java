package org.peerbox.app.manager;

import java.util.concurrent.Future;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

public class ProcessHandle<T> {
	
	private IProcessComponent<T> process;
	private Future<T> future;
	
	public ProcessHandle(IProcessComponent<T> processHandle) {
		this.process = processHandle;
	}
	
	public IProcessComponent<T> getProcess() {
		return process;
	}
	
	public Future<T> getFuture() {
		return future;
	}
	
	public T execute() throws InvalidProcessStateException, ProcessExecutionException {
		return process.execute();
	}
	
	public void executeAsync() throws InvalidProcessStateException, ProcessExecutionException {
		future = process.executeAsync();
	}
}

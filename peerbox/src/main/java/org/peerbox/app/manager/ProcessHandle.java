package org.peerbox.app.manager;

import java.util.concurrent.Future;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * The ProcessHandle is a handle for a {@link IProcessComponent} returned by H2H.
 * It just wraps the process component and stores the {@link Future} of async execution.
 *
 * Note: use {@link #execute()} or {@link #executeAsync()} of this
 * handle instead of the methods of the process itself. Otherwise, the returned
 * Future reference is not stored.
 *
 * @author albrecht
 *
 * @param <T> return type of the process
 */
public class ProcessHandle<T> {

	/* process as returned by H2H */
	private IProcessComponent<T> process;
	/* future of async operation - set if executeAsync is called. */
	private Future<T> future;

	/**
	 * Creates a new process handle instance.
	 *
	 * @param process provided by H2H
	 */
	public ProcessHandle(IProcessComponent<T> process) {
		this.process = process;
	}

	/**
	 * Returns the process component
	 *
	 * @return H2H process
	 */
	public IProcessComponent<T> getProcess() {
		return process;
	}

	/**
	 * Returns the {@link Future} of the process in the case of async execution.
	 * Future is available as soon as {@link #executeAsync()} is called.
	 *
	 * @return future handle
	 */
	public Future<T> getFuture() {
		return future;
	}

	/**
	 * Synchronous execution of the process.
	 * Executes the process by calling {@link IProcessComponent#execute()}.
	 *
	 * @return return value of process (if any)
	 * @throws InvalidProcessStateException
	 * @throws ProcessExecutionException
	 */
	public T execute() throws InvalidProcessStateException, ProcessExecutionException {
		return process.execute();
	}

	/**
	 * Asynchronous execution of the process.
	 * Executes the process by calling {@link IProcessComponent#executeAsync()}.
	 * Note: future is set ({@link #executeAsync()}.
	 *
	 * @throws InvalidProcessStateException
	 * @throws ProcessExecutionException
	 */
	public void executeAsync() throws InvalidProcessStateException, ProcessExecutionException {
		future = process.executeAsync();
	}
}

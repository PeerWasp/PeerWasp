package org.peerbox.watchservice;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hive2hive.core.exceptions.AbortModificationCode;
import org.hive2hive.core.exceptions.ErrorCode;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.h2h.ProcessHandle;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.peerbox.watchservice.states.ExecutionHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * The FileActionExecutor service observes a set of file actions in a queue.
 * An action is executed as soon as it is considered to be "stable", i.e. no more events were 
 * captured within a certain period of time.
 * 
 * @author albrecht
 *
 */
public class ActionExecutor implements Runnable, IActionEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
	
	/**
	 *  amount of time that an action has to be "stable" in order to be executed 
	 */
	public static final long ACTION_WAIT_TIME_MS = 2000;
	public static final int NUMBER_OF_EXECUTE_SLOTS = 10;
	public static final int MAX_EXECUTION_ATTEMPTS = 3;
	
	private FileManager fileManager;
	private FileEventManager fileEventManager;
	private boolean waitForActionCompletion = true;
	
	private BlockingQueue<ExecutionHandle> asyncHandles; 
	private Vector<IAction> runningJobs;
	private Thread asyncHandlesThread;

	private Thread executorThread;
	
	@Inject
	public ActionExecutor(FileEventManager eventManager, FileManager fileManager){
		this.fileEventManager = eventManager;
		this.fileManager = fileManager;
		
		asyncHandles = new LinkedBlockingQueue<ExecutionHandle>();
		runningJobs = new Vector<IAction>();
		asyncHandlesThread = new Thread(new AsyncActionHandler(), "AsyncActionHandlerThread");
		
        executorThread = new Thread(this, "ActionExecutorThread");
	}
	
	public void start() {
		executorThread.start();
	}
	
	public void stop() {
		// TODO: change stop to something that is not deprecated and recommended.
		executorThread.stop();
	}
	
	public void setWaitForActionCompletion(boolean wait) {
		this.waitForActionCompletion = wait;
	}
	
	public BlockingQueue<ExecutionHandle> getFailedJobs() {
		return asyncHandles;
	}
	
	public Vector<IAction> getRunningJobs() {
		return runningJobs;
	}
	

	@Override
	public void run() {
		asyncHandlesThread.start();
		processActions();
	}

	/**
	 * Processes the action in the action queue, one by one.
	 * @throws IllegalFileLocation 
	 * @throws NoPeerConnectionException 
	 * @throws NoSessionException 
	 */
	private synchronized void processActions() {
		while (true) {
			try {
				FileComponent next = null;
				next = fileEventManager.getFileComponentQueue().take();
				if(!isFileComponentReady(next)){
					logger.debug("{} is not ready yet!", next.getPath());
					fileEventManager.getFileComponentQueue().remove(next);
					next.getAction().updateTimestamp();
					fileEventManager.getFileComponentQueue().add(next);
					continue;
				}
				if (isTimerReady(next.getAction()) && isExecuteSlotFree()) {

//					if (next.getAction().getCurrentState() instanceof LocalDeleteState) {
						removeFromDeleted(next);
//					}
					
					next.getAction().addEventListener(this);
					
					logger.debug("Start execution: {}", next.getPath());
					ExecutionHandle ehandle = next.getAction().execute(fileManager);
					if(waitForActionCompletion){
						if(ehandle != null && ehandle.getProcessHandle() != null) {
							logger.debug("Put into async handles!");
//							asyncHandles.put(ehandle);
							runningJobs.add(next.getAction());
							
						}
					} else {
						onActionExecuteSucceeded(next.getAction());
					}
				} else {
					if(!isExecuteSlotFree()){
						logger.debug("All slots used!");
						for(int i = 0; i < runningJobs.size(); i++){
							logger.trace("{}: {} {}", i, runningJobs.get(i).getFilePath(), runningJobs.get(i).getCurrentState().getClass());
						}
					}
					fileEventManager.getFileComponentQueue().put(next);
					long timeToWait = calculateWaitTime(next);
					wait(timeToWait);
				}
				next = fileEventManager.getFileComponentQueue().take();
				fileEventManager.getFileComponentQueue().put(next);
			} catch (InterruptedException iex) {
				iex.printStackTrace();
				//return;
			} catch (Throwable t){
				logger.error("Throwable occurred: {}", t.getMessage());
				for(int i = 0; i < t.getStackTrace().length; i++){
					StackTraceElement curr = t.getStackTrace()[i];
					logger.error("{} : {} ", curr.getClassName(), curr.getMethodName());
					logger.error("{} : {} ", curr.getFileName(), curr.getLineNumber());
				}
			}
		}
	}


	private boolean isFileComponentReady(FileComponent next) {
		logger.debug("Check if ready!");
		return next.isReady();
	}

	private boolean isExecuteSlotFree() {
		return runningJobs.size() < NUMBER_OF_EXECUTE_SLOTS;
	}


	private long calculateWaitTime(FileComponent action) {
		long timeToWait = ACTION_WAIT_TIME_MS - getActionAge(action.getAction()) + 1L;
		if(timeToWait < 500L) { // wait at least some time
			timeToWait = 500L;
		}
		return timeToWait;
	}


	private void removeFromDeleted(FileComponent next) {
		Set<FileComponent> sameHashDeletes = fileEventManager.getFileTree().getDeletedByContentHash().get(next.getContentHash());
		Iterator<FileComponent> componentIterator = sameHashDeletes.iterator();
		while(componentIterator.hasNext()){
			FileComponent candidate = componentIterator.next();
			if(candidate.getPath().toString().equals(next.getPath().toString())){
				componentIterator.remove();
				break;
			}
		}
		Map<String, FolderComposite> deletedByContentNamesHash = fileEventManager.getFileTree().getDeletedByContentNamesHash();
		if(next instanceof FolderComposite){
			FolderComposite nextAsFolder = (FolderComposite)next;
			deletedByContentNamesHash.remove(nextAsFolder.getContentNamesHash());
		}

	}
	
	/**
	 * Checks whether an action is ready to be executed
	 * @param action Action to be executed
	 * @return true if ready to be executed, false otherwise
	 */
	private boolean isTimerReady(IAction action) {
		long ageMs = getActionAge(action);
		return ageMs >= ACTION_WAIT_TIME_MS;
	}
	
	/**
	 * Computes the age of an action
	 * @param action
	 * @return age in ms
	 */
	private long getActionAge(IAction action) {
		return System.currentTimeMillis() - action.getTimestamp();
	}


	@Override
	public void onActionExecuteSucceeded(IAction action) {
		logger.debug("Action successful: {} {}.", action.getFilePath(), action.getCurrentState().getClass().toString());
//		for(int i = 0; i < executingActions.size(); i++){
//			IAction a = executingActions.get(i);
//			logger.trace("{}     Action {} with ID {} and State {}", i, a.getFilePath(), a.getFilePath().hashCode(), a.getCurrentState().getClass());
//		}
		boolean hasChanged = runningJobs.remove(action);
		if(!hasChanged){
			logger.error("The action was successful, but the running jobs did not"
					+ "change on its remove: {}", action.getFilePath());
		} else {
			logger.debug("Action {} with state {} and ID {} removed", action.getFilePath(), action.getCurrentState().getClass(), action.hashCode());
		}
		
		boolean changedWhileExecuted = false;
		
		logger.trace("Wait for lock of action {} at {}", action.getFilePath(), System.currentTimeMillis());
		action.getLock().lock();
		logger.trace("Received lock of action {} at {}", action.getFilePath(), System.currentTimeMillis());
		changedWhileExecuted = action.getChangedWhileExecuted();
		
		action.onSucceed();
		action.setIsUploaded(true);
		logger.trace("Release lock of action {} at {}", action.getFilePath(), System.currentTimeMillis());
		
		if(changedWhileExecuted){
			logger.trace("File {} changed during the execution process"
					+ " to state {}. It has been put back to the queue", 
					action.getFilePath(), action.getCurrentState().getClass());
			action.updateTimestamp();
			fileEventManager.getFileComponentQueue().add(action.getFile());
			
		}

		//logger.debug("Action successful: {} {} {}", action.getFilePath(), action.hashCode(), action.getCurrentState().getClass().toString());
		
		action.getLock().unlock();		
	}


	@Override
	public void onActionExecuteFailed(IAction action, ProcessHandle<Void> handle) {
		logger.debug("Action failed: {} {}.", action.getFilePath(), action.getCurrentState().getClass().toString());
		try {
			boolean hasChanged = runningJobs.remove(action);
			asyncHandles.put(new ExecutionHandle(action, handle));
		} catch (InterruptedException e) {
			logger.warn("Could not put failed item into queue.");
		}
	}
	
	
	private void handleExecutionError(IAction action, ProcessExecutionException pex) {
		Hive2HiveException h2hex = (Hive2HiveException) pex.getCause();
		if(pex != null && pex.getCause() != null && h2hex.getError() != null) {
			/*
			 * TODO: unwrap exception and handle inner exception that caused ProcesssExecutionException
			 * 
			 * Previous error codes: 
			 * - PARENT_IN_USERFILE_NOT_FOUND --> [ ParentInUserProfileNotFoundException ]
			 * - SAME_CONTENT --> [ NewVersionSameContentException ]
			 * - ?? rest was not used I think
			 */
			

			

//			if(h2hex != null && h2hex.getError() != null){
			ErrorCode error = h2hex.getError();
			if(error == AbortModificationCode.SAME_CONTENT){
				logger.debug("H2H update of file {} failed, content hash did not change. {}", action.getFilePath(), fileEventManager.getFileTree().getRootPath().toString());
//				FileComponent notModified = fileEventManager.getFileTree().getComponent(action.getFilePath().toString());
				FileComponent notModified = fileEventManager.getFileTree().getFile(action.getFilePath());
				if(notModified == null){
					logger.trace("FileComponent with path {} is null", action.getFilePath().toString());
				}
				action.onSucceed();
			} else if(error == AbortModificationCode.FOLDER_UPDATE){
				logger.debug("Attempt to update folder {} failed as folder cannot be updated.", action.getFilePath());
			} else if(error == AbortModificationCode.ROOT_DELETE_ATTEMPT){
				logger.debug("Attempt to delete the root folder {} failed. Delete is not defined on this folder.", action.getFilePath());
			} else if(error == AbortModificationCode.NO_WRITE_PERM){
				logger.debug("Attempt to delete or write to {} failed. No write-permissions hold by user.", action.getFilePath());
			} else {
				logger.trace("Re-initiate execution of {} {}.", action.getFilePath(), action.getCurrentState().getClass().toString());
				if(action.getExecutionAttempts() <= MAX_EXECUTION_ATTEMPTS){
					action.updateTimestamp();
					fileEventManager.getFileComponentQueue().add(action.getFile());
				} else {
					logger.error("To many attempts, action of {} has not been executed again. Reason: default", action.getFilePath());
					onActionExecuteSucceeded(action);
				}
			}
//			}
		} else {
			// temporary default
			logger.trace("Default: Re-initiate execution of {} {}.", action.getFilePath(), action.getCurrentState().getClass().toString());
			if(action.getExecutionAttempts() <= MAX_EXECUTION_ATTEMPTS){
				action.updateTimestamp();
				fileEventManager.getFileComponentQueue().add(action.getFile());
			} else {
				logger.error("To many attempts, action of {} has not been executed again. Reason: default", action.getFilePath());
				onActionExecuteSucceeded(action);
			}
		}	
	}

	
	private class AsyncActionHandler implements Runnable {

		@Override
		public void run() {
			processFailedActions();
		}

		private void processFailedActions() {
			while(true) {
				try {
					
					ExecutionHandle next = asyncHandles.take();
					
					try {
						// get should be ready because onFailed event already happened,
						// but we do not want to block forever!
						next.getProcessHandle().getFuture().get(5, TimeUnit.SECONDS);
					} catch(ExecutionException eex) {
						
						ProcessExecutionException pex = null;
						if(eex.getCause() instanceof ProcessExecutionException) {
							pex = (ProcessExecutionException)eex.getCause();
						} 
						handleExecutionError(next.getAction(), pex);
						
					} catch(CancellationException | InterruptedException e) {
						logger.warn("Exception while getting future result: {}", e.getMessage());
					} catch(TimeoutException tex) {
						logger.debug("Could not get result of failed item, timed out. {}", next.getAction().getFilePath());
						// add it again 
						asyncHandles.put(next);
					}
					// if this point reached, no error occurred (get() did not throw exception)
					
				} catch(Exception e) {
					logger.warn("Exception in processFailedActions: ", e);
				}
			}
		}
		
	}
}

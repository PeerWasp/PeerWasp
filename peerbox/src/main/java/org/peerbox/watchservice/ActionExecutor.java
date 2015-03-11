package org.peerbox.watchservice;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.exceptions.AbortModificationCode;
import org.hive2hive.core.exceptions.ErrorCode;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.IPeerWaspConfig;
import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.events.IMessage;
import org.peerbox.forcesync.ForceSyncCompleteMessage;
import org.peerbox.forcesync.ForceSyncMessage;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionStartedMessage;
import org.peerbox.presenter.settings.synchronization.messages.FileExecutionSucceededMessage;
import org.peerbox.view.tray.SynchronizationCompleteNotification;
import org.peerbox.view.tray.SynchronizationErrorsResolvedNotification;
import org.peerbox.view.tray.SynchronizationStartsNotification;
import org.peerbox.watchservice.filetree.IFileTree;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.peerbox.watchservice.filetree.composite.FolderComposite;
import org.peerbox.watchservice.states.ExecutionHandle;
import org.peerbox.watchservice.states.LocalMoveState;
import org.peerbox.watchservice.states.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;

/**
 * The ActionExecutor service conducts the aggregation of events for
 * every file and folder and uses a {@link peerbox.src.main.java
 * .org.peerbox.watchservice.ActionQueue ActionQueue} for this purpose, which
 * runs in a separate thread. Ready actions, for which no new events
 * have been observed for a specified time span, are executed. The class
 * maintains asynchronous handles for ongoing executions to check whether
 * they conclude successfully or not and react accordingly.
 *
 * This class uses the {@link org.peerbox.events.MessageBus MessageBus} instance of the
 * injected {@link org.peerbox.watchservice.FileEventManager FileEventManager}
 * instance to publish {@link org.peerbox.view.tray.SynchronizationErrorsResolvedNotification SynchronizationErrorsResolvedNotification},
 * {@link org.peerbox.presenter.settings.synchronization.messages.FileExecutionStartedMessage FileExecutionStartedMessage},
 * {@link org.peerbox.presenter.settings.synchronization.messages.FileExecutionSucceededMessage FileExecutionSucceededMessage},
 * {@link org.peerbox.app.manager.file.FileExecutionFailedMessage FileExecutionFailedMessage}, and
 * {@link org.peerbox.view.tray.SynchronizationCompleteNotification SynchronizationCompleteNotification}.

 * @author albrecht
 *
 */
public class ActionExecutor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);

	private IPeerWaspConfig peerWaspConfig;

	private final IFileManager fileManager;
	private final FileEventManager fileEventManager;

	/** Set to false if not interested in result of network transactions **/
	private boolean waitForActionCompletion = true;

	/** Queue to store the handles of executing transactions, ending
	 * transactions are examined asynchronously **/
	private final BlockingQueue<ExecutionHandle> asyncHandles;

	private final Thread asyncHandlesThread;
	private final Thread executorThread;

	private boolean forceSyncRunning = false;

	/**
	 * @param eventManager determines the event handling
	 * @param fileManager is passed when actions are executed to access the H2H API.
	 * @param peerWaspConfig defines important runtime parameters like
	 * the number of concurrently executed actions or the aggregation
	 * time span for events.
	 */
	@Inject
	public ActionExecutor(final FileEventManager eventManager,
			final IFileManager fileManager,
			IPeerWaspConfig peerWaspConfig) {

		this.fileEventManager = eventManager;
		this.fileManager = fileManager;
		this.peerWaspConfig = peerWaspConfig;

		asyncHandles = new LinkedBlockingQueue<ExecutionHandle>();

		asyncHandlesThread = new Thread(new ExecutingActionHandler(), "AsyncActionHandlerThread");
		executorThread = new Thread(this, "ActionExecutorThread");
	}


	public void start() {
		executorThread.start();
	}

	public void stop() {
		// TODO: change stop to something that is not deprecated and recommended.
		executorThread.stop();
	}

	@Override
	public void run() {
		asyncHandlesThread.start();
		processActions();
	}

	public IPeerWaspConfig getPeerWaspConfig(){
		return peerWaspConfig;
	}

	/**
	 * Processes the actions in the action queue, one by one. For each action,
	 * the thread checks if a slot is free (i.e. the upper bound of concurrent
	 * executions is not reached) and if the timestamp (updated on every event)
	 * of the action is old enough to conclude the event aggregation for this
	 * action. Besides that, the method checks if the ancestors of a file have
	 * been uploaded to the network yet, to prevent a {@link org.hive2hive.core.
	 * src.main.java.org.hive2hive.core.exceptions.ParentInUserProfileNotFoundException}
	 * @throws IllegalFileLocation
	 * @throws NoPeerConnectionException
	 * @throws NoSessionException
	 */
	private synchronized void processActions() {
		while (true) {

			FileComponent next = null;

			try {

				next = fileEventManager.getFileComponentQueue().take();
				if (!isFileComponentReady(next)) {
					updateFileComponentQueueAndWait(next);
					continue;
				}

				if (isTimerReady(next.getAction()) && isExecuteSlotFree()) {

					removeFromDeleted(next);
					removeFromCreated(next);
					removeFromFailed(next.getPath());

					logger.debug("Start execution: {}", next.getPath());

					ExecutionHandle ehandle = next.getAction().execute(fileManager);
					if (waitForActionCompletion) {

						if (ehandle != null && ehandle.getProcessHandle() != null) {
							logger.debug("Put into async handles!");
							asyncHandles.put(ehandle);
							FileHelper file = new FileHelper(next.getPath(), next.isFile());
							publishMessage(new FileExecutionStartedMessage(file, next.getAction().getCurrentState().getStateType()));
						} else {
							//This happens with actions in InitialState/EstablishedState
							FileHelper file = new FileHelper(next.getPath(), next.isFile());
							publishMessage(new FileExecutionSucceededMessage(file, next.getAction().getCurrentState().getStateType()));
						}

						if(asyncHandles.size() != 0){
							publishMessage(new SynchronizationStartsNotification());
						}
					} else {
						onActionExecuteSucceeded(next.getAction());
					}
				} else {
					if (!isExecuteSlotFree()) {
						logger.debug("All slots used! Current jobs: ");
						logRunningJobs();
					}
					fileEventManager.getFileComponentQueue().add(next);
					wait(calculateWaitTime(next));
				}
			} catch (InterruptedException iex) {
				logger.error("Exception occurred: {}", iex.getMessage(), iex);
			} catch (NoSessionException nse) {
				logger.warn("No session - cannot execute pending actions.", nse);
			} catch(NoPeerConnectionException npc) {
				logger.warn("No peer connection - cannot execute pending actions.", npc);
			} catch (Exception e) {
				logger.error("Exception occurred: {}", e.getMessage(), e);
			}
		}
	}

	private void updateFileComponentQueueAndWait(FileComponent next) throws InterruptedException{
		logger.debug("Component {} is not ready yet!", next.getPath());

		fileEventManager.getFileComponentQueue().remove(next);
		next.getAction().updateTimestamp();
		fileEventManager.getFileComponentQueue().add(next);

		wait(calculateWaitTime(next));
	}

	private boolean isFileComponentReady(FileComponent next) {
		return next.isReady();
	}

	/**
	 * Checks whether an action is ready to be executed
	 * @param action Action to be executed
	 * @return true if ready to be executed, false otherwise
	 */
	private boolean isTimerReady(IAction action) {
		long ageMs = getActionAge(action);
		return ageMs >= peerWaspConfig.getAggregationIntervalInMillis();
	}

	/**
	 * Computes the age of an action
	 * @param action
	 * @return age in ms
	 */
	private long getActionAge(IAction action) {
		return System.currentTimeMillis() - action.getTimestamp();
	}

	private long calculateWaitTime(FileComponent action) {
		long timeToWait = peerWaspConfig.getAggregationIntervalInMillis() - getActionAge(action.getAction()) + 1L;
		if (timeToWait < 500L) {
			// wait at least some time
			timeToWait = 500L;
		}
		return timeToWait;
	}

	private boolean isExecuteSlotFree() {
		return asyncHandles.size() < peerWaspConfig.getNumberOfExecutionSlots();
	}

	public void setWaitForActionCompletion(boolean wait) {
		this.waitForActionCompletion = wait;
	}

	public BlockingQueue<ExecutionHandle> getFailedJobs() {
		return asyncHandles;
	}

	/**
	 * @return the file tree
	 */
	private IFileTree getFileTree() {
		return fileEventManager.getFileTree();
	}

	private void logRunningJobs() {
		Iterator<ExecutionHandle> it = asyncHandles.iterator();
		int index = 0;
		while (it.hasNext()) {
			ExecutionHandle next = it.next();
			IAction tmpAction = next.getAction();
			logger.trace("[{}] {} - {}", index,
					tmpAction.getCurrentStateName(),
					tmpAction.getFile().getPath());
			++index;
		}
	}

	private void removeFromDeleted(FileComponent next) {
		SetMultimap<String, FileComponent> deletedByContent = getFileTree().getDeletedByContentHash();
		SetMultimap<String, FolderComposite> deletedByStructure = getFileTree().getDeletedByStructureHash();
		removeComponentFromSetMultimap(next, deletedByContent, deletedByStructure);
	}

	private void removeFromCreated(FileComponent next){
		SetMultimap<String, FileComponent> createdByContent = getFileTree().getCreatedByContentHash();
		SetMultimap<String, FolderComposite> createdByStructure = getFileTree().getCreatedByStructureHash();
		removeComponentFromSetMultimap(next, createdByContent, createdByStructure);
	}

	private void removeFromFailed(Path failedOperation) {
		fileEventManager.getFailedOperations().remove(failedOperation);

		if (fileEventManager.getFailedOperations().size() == 0) {
			publishMessage(new SynchronizationErrorsResolvedNotification());
		}
	}

	private void publishMessage(IMessage message) {
		if (fileEventManager.getMessageBus() != null) {
			fileEventManager.getMessageBus().publish(message);
		}
	}

	private void removeComponentFromSetMultimap(FileComponent toRemove,
			SetMultimap<String, FileComponent> byContent,
			SetMultimap<String, FolderComposite> byStructure){
		Iterator<Map.Entry<String, FileComponent>> componentIterator = byContent.entries().iterator(); //.sameHashes.iterator();

		if(toRemove.isFile()){
			synchronized(byContent){
				while(componentIterator.hasNext()){
					FileComponent candidate = componentIterator.next().getValue();
					if(candidate.getPath().toString().equals(toRemove.getPath().toString())){
						componentIterator.remove();
						break;
					}
					if(System.currentTimeMillis() - candidate.getAction().getTimestamp() > peerWaspConfig.getAggregationIntervalInMillis()){
						logger.trace("Remove old entry: {}", candidate.getPath());
						componentIterator.remove();
					}
				}
			}
		} else {
			Iterator<Map.Entry<String, FolderComposite>> folderIterator = byStructure.entries().iterator();
			synchronized(byStructure){
				while(folderIterator.hasNext()){
					Map.Entry<String, FolderComposite> candidate = folderIterator.next();
					if(candidate.getValue().getPath().toString().equals(toRemove.getPath().toString())){
						folderIterator.remove();
						break;
					}
				}
			}
		}
	}

	private void onActionExecuteSucceeded(final IAction action) {
		final FileComponent file = action.getFile();
		logger.debug("Action succeeded: {} {}.",
				file.getPath(), action.getCurrentStateName());

		//inform GUI to adjust icon
		FileHelper fileHelper = new FileHelper(file.getPath(), file.isFile());
		if(action.getCurrentState().getStateType() == StateType.LOCAL_MOVE){
			LocalMoveState state = (LocalMoveState)action.getCurrentState();
			FileHelper source = new FileHelper(state.getSourcePath(), file.isFile());
			publishMessage(new FileExecutionSucceededMessage(source, fileHelper, action.getCurrentState().getStateType()));
		} else {
			publishMessage(new FileExecutionSucceededMessage(fileHelper, action.getCurrentState().getStateType()));
		}

		boolean changedWhileExecuted = action.getChangedWhileExecuted();
		file.setIsUploaded(true);
		action.onSucceeded();

		if (changedWhileExecuted) {
			logger.trace("File: {} changed during the execution process to state {}. "
					+ "Put back into the queue",
					file.getPath(),
					action.getCurrentStateName());
			action.updateTimestamp();
			fileEventManager.getFileComponentQueue().add(file);
		}
	}


	private void handleExecutionError(IAction action, ProcessExecutionException pex) {
		final FileComponent file = action.getFile();
		logger.error("Action failed: {}", file.getPath(), pex);

		action.onFailed();

		boolean errorHandled = false;
		if (pex != null && pex.getCause() != null) {
			if (pex.getCause() instanceof Hive2HiveException) {
				Hive2HiveException h2hex = (Hive2HiveException) pex.getCause();
				if (h2hex.getError() != null) {
					ErrorCode error = h2hex.getError();
					errorHandled = handleErrorByCode(action, error);
				}
			}
		}
		if (!errorHandled) {
			handleErrorDefault(action);
		}
	}


	private void handleErrorDefault(IAction action) {
		final Path path = action.getFile().getPath();
		logger.trace("Default Error Handling: Re-initiate execution - {} - {} - attempt({}).",
				path, action.getCurrentStateName(), action.getExecutionAttempts());

		if (action.getExecutionAttempts() <= peerWaspConfig.getMaximalExecutionAttempts()) {
			action.updateTimestamp();
			fileEventManager.getFileComponentQueue().add(action.getFile());
		} else {
			FileHelper file = new FileHelper(path, action.getFile().isFile());
			publishMessage(new FileExecutionFailedMessage(file));
			fileEventManager.getMessageBus().post(new InformationNotification("Synchronization error ",
					"Operation on " + path + " failed")).now();
			logger.error("To many attempts, action of {} has not been executed again.", path);
			onActionExecuteSucceeded(action);
			fileEventManager.getFailedOperations().add(action.getFile().getPath());
		}
	}

	private boolean handleErrorByCode(IAction action, ErrorCode error) {
		final Path path = action.getFile().getPath();

		if (error == AbortModificationCode.SAME_CONTENT) {

			logger.debug("Update of file {} failed, content hash did not change", path);
			FileComponent notModified = getFileTree().getFile(path);
			if (notModified == null) {
				logger.trace("FileComponent not found (null): {}", path);
			}
			FileHelper file = new FileHelper(action.getFile().getPath(), action.getFile().isFile());
			publishMessage(new FileExecutionSucceededMessage(file, action.getCurrentState().getStateType()));
			action.onSucceeded();
			return true;

		} else if (error == AbortModificationCode.FOLDER_UPDATE) {
			logger.debug("Attempt to update folder {} failed as folder cannot be updated.", path);
			return true;

		} else if (error == AbortModificationCode.ROOT_DELETE_ATTEMPT) {
			logger.debug("Attempt to delete the root folder {} failed (operation not allowed)", path);
			return true;

		} else if (error == AbortModificationCode.NO_WRITE_PERM) {
			//This happens when a user creates a file in a read-only folder.
			logger.debug("Attempt to delete or write to {} failed. No write-permissions.", path);
			return true;
		}
		return false; // error not handled
	}

	@Handler
	public void onForceSync(ForceSyncMessage message){
		logger.trace("Force Synchronization on {}: Handle ongoing executions");
		setForceSyncRunning(true);
	}

	@Handler
	public void onForceSyncComplete(ForceSyncCompleteMessage message){
		logger.trace("Forced synchronization terminated.");
		setForceSyncRunning(false);
	}

	public void setForceSyncRunning(boolean isRunning) {
		forceSyncRunning  = isRunning;
	}

	private class ExecutingActionHandler implements Runnable {

		@Override
		public void run() {
			processExecutingActions();
		}

		private void processExecutingActions() {
			while(true) {
				try {

					ExecutionHandle next = asyncHandles.take();

					try {
						if(forceSyncRunning){
							logger.trace("FileComponent {} in state {} is discarded due to force sync!",
									next.getAction().getFile().getPath(), next.getAction().getCurrentState().getStateType());
							continue;
						}
						// check whether there is a process attached
						ProcessHandle<Void> process = next.getProcessHandle();
						if(process != null) {
							process.getFuture().get(5, TimeUnit.SECONDS);
						}

						onActionExecuteSucceeded(next.getAction());

						if(asyncHandles.size() == 0){
							publishMessage(new SynchronizationCompleteNotification());
						}
					} catch (ExecutionException eex) {

						ProcessExecutionException pex = null;
						if (eex.getCause() instanceof ProcessExecutionException) {
							pex = (ProcessExecutionException) eex.getCause();
						}
						handleExecutionError(next.getAction(), pex);

					} catch (CancellationException | InterruptedException e) {
						logger.warn("Exception while getting future result: {}", e.getMessage());
					} catch (TimeoutException tex) {
						logger.debug("Could not get result of failed item, timed out. {}",
								next.getAction().getFile().getPath());
						// add it again and try later
						asyncHandles.put(next);
					}

				} catch (Exception e) {
					logger.warn("Exception in processFailedActions: ", e);
				}
			}
		}
	}
}

package org.peerbox.watchservice.filetree.persistency;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * This class is responsible for periodically storing metadata about files.
 * The remote user profile as well as the local file tree is persisted in a database once in a while.
 *
 * @author albrecht
 *
 */
public class PeriodicFileDataPersister {

	private static final Logger logger = LoggerFactory.getLogger(PeriodicFileDataPersister.class);

	private ScheduledExecutorService scheduler;

	// scheduling delay in seconds
	private static final long SCHEDULED_DELAY = 30L;
	private static final long INITIAL_DELAY = 0L;

	private Provider<PersistLocalTree> localTaskProvider;
	private Provider<PersistRemoteProfile> remoteTaskProvider;

	// persistence tasks
	private PersistLocalTree persistTaskLocal;
	private PersistRemoteProfile persistTaskRemote;

	@Inject
	public PeriodicFileDataPersister(Provider<PersistLocalTree> localTaskProvider,
			Provider<PersistRemoteProfile> remoteTaskProvider) {
		this.localTaskProvider = localTaskProvider;
		this.remoteTaskProvider = remoteTaskProvider;

	}

	/**
	 * Schedules persistence tasks. The tasks run periodically with a fixed delay.
	 */
	public void start() {
		scheduler = Executors.newScheduledThreadPool(1);

		persistTaskLocal = localTaskProvider.get();
		persistTaskRemote = remoteTaskProvider.get();

		scheduler.scheduleWithFixedDelay(persistTaskLocal,
				INITIAL_DELAY, SCHEDULED_DELAY, TimeUnit.SECONDS);
		scheduler.scheduleWithFixedDelay(persistTaskRemote,
				INITIAL_DELAY, SCHEDULED_DELAY, TimeUnit.SECONDS);
	}

	/**
	 * Stops persistence tasks. This method may block for a short while because
	 * it tries to wait until running tasks complete.
	 */
	public void stop() {
		if (scheduler != null) {
			try {
				scheduler.shutdown();
				scheduler.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.info("Could not wait for scheduler termination (profile persistence)");
			} finally {
				scheduler = null;
				persistTaskLocal = null;
				persistTaskRemote = null;
			}
		}
	}

}

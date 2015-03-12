package org.peerbox.app.activity.collectors;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.LocalFileConflictMessage;
import org.peerbox.app.manager.file.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.RemoteFileAddedMessage;
import org.peerbox.app.manager.file.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.RemoteFileMovedMessage;
import org.peerbox.presenter.settings.synchronization.FileHelper;

public class FileManagerCollectorTest extends BaseJUnitTest {

	private FileManagerCollector collector;
	private ActivityLogger activityLogger;

	@Before
	public void setUp() throws Exception {
		activityLogger = Mockito.mock(ActivityLogger.class);
		collector = new FileManagerCollector(activityLogger);
	}

	@After
	public void tearDown() throws Exception {
		activityLogger = null;
		collector = null;
	}

	@Test
	public void testFileManagerCollector() {
		assertEquals(collector.getActivityLogger(), activityLogger);
	}

	@Test
	public void testOnFileUploaded() {
		FileHelper file = new FileHelper(Paths.get("this/is/a/path.txt"), true);
		RemoteFileAddedMessage message = new RemoteFileAddedMessage(file);
		collector.onRemoteFileAdded(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDownloaded() {
		FileHelper file = new FileHelper(Paths.get("this/is/a/path.txt"), true);
		FileHelper dstFile = new FileHelper(Paths.get("this/is/another/path.txt"), true);
		RemoteFileMovedMessage message = new RemoteFileMovedMessage(file, dstFile);
		collector.onRemoteFileMoved(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDeleted() {
		FileHelper file = new FileHelper(Paths.get("this/is/a/path.txt"), true);
		RemoteFileDeletedMessage message = new RemoteFileDeletedMessage(file);
		collector.onRemoteFileDeleted(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileConfilct() {
		FileHelper file = new FileHelper(Paths.get("this/is/a/path.txt"), true);
		LocalFileConflictMessage message = new LocalFileConflictMessage(file);
		collector.onLocalFileConfilct(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.WARNING, activityLogger);
	}

	@Test
	public void testOnFileDesynchronized(){
		FileHelper file = new FileHelper(Paths.get("this/is/a/path.txt"), true);
		LocalFileDesyncMessage message = new LocalFileDesyncMessage(file);
		collector.onLocalFileDesynchronized(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileExecutionFailed(){
		FileHelper file = new FileHelper(Paths.get("this/is/a/path.txt"), true);
		FileExecutionFailedMessage message = new FileExecutionFailedMessage(file);
		collector.onFileExecutionFailed(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.ERROR, activityLogger);
	}

}

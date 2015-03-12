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
import org.peerbox.app.manager.file.FileInfo;
import org.peerbox.app.manager.file.messages.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.messages.LocalFileConflictMessage;
import org.peerbox.app.manager.file.messages.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.messages.RemoteFileAddedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.messages.RemoteFileMovedMessage;

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
		FileInfo file = new FileInfo(Paths.get("this/is/a/path.txt"), false);
		RemoteFileAddedMessage message = new RemoteFileAddedMessage(file);
		collector.onRemoteFileAdded(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDownloaded() {
		FileInfo file = new FileInfo(Paths.get("this/is/a/path.txt"), false);
		FileInfo dstFile = new FileInfo(Paths.get("this/is/another/path.txt"), false);
		RemoteFileMovedMessage message = new RemoteFileMovedMessage(file, dstFile);
		collector.onRemoteFileMoved(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDeleted() {
		FileInfo file = new FileInfo(Paths.get("this/is/a/path.txt"), false);
		RemoteFileDeletedMessage message = new RemoteFileDeletedMessage(file);
		collector.onRemoteFileDeleted(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileConfilct() {
		FileInfo file = new FileInfo(Paths.get("this/is/a/path.txt"), false);
		LocalFileConflictMessage message = new LocalFileConflictMessage(file);
		collector.onLocalFileConfilct(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.WARNING, activityLogger);
	}

	@Test
	public void testOnFileDesynchronized(){
		FileInfo file = new FileInfo(Paths.get("this/is/a/path.txt"), false);
		LocalFileDesyncMessage message = new LocalFileDesyncMessage(file);
		collector.onLocalFileDesynchronized(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileExecutionFailed(){
		FileInfo file = new FileInfo(Paths.get("this/is/a/path.txt"), false);
		FileExecutionFailedMessage message = new FileExecutionFailedMessage(file);
		collector.onFileExecutionFailed(message);

		CollectorTestUtils.captureAddActivityItem(ActivityType.ERROR, activityLogger);
	}

}

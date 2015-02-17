package org.peerbox.app.activity.collectors;

import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.app.activity.ActivityLogger;
import org.peerbox.app.activity.ActivityType;
import org.peerbox.app.manager.file.LocalFileConflictMessage;
import org.peerbox.app.manager.file.RemoteFileDeletedMessage;
import org.peerbox.app.manager.file.LocalFileDesyncMessage;
import org.peerbox.app.manager.file.RemoteFileMovedMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.RemoteFileAddedMessage;

public class FileManagerCollectorTest extends BaseJUnitTest {

	private FileManagerCollector collector;
	private ActivityLogger activityLogger;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

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

	@Test @Ignore
	public void testFileManagerCollector() {
		fail("Not yet implemented");
	}

	@Test
	public void testOnFileUploaded() {
		RemoteFileAddedMessage message = new RemoteFileAddedMessage(Paths.get("this/is/a/path.txt"));
		collector.onRemoteFileAdded(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDownloaded() {
		RemoteFileMovedMessage message = new RemoteFileMovedMessage(Paths.get("this/is/a/path.txt"), Paths.get("this/is/another/path.txt"));
		collector.onRemoteFileMoved(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDeleted() {
		RemoteFileDeletedMessage message = new RemoteFileDeletedMessage(Paths.get("this/is/a/path.txt"));
		collector.onRemoteFileDeleted(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileConfilct() {
		LocalFileConflictMessage message = new LocalFileConflictMessage(Paths.get("this/is/a/path.txt"));
		collector.onLocalFileConfilct(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.WARNING, activityLogger);
	}
	
	@Test
	public void testOnFileDesynchronized(){
		LocalFileDesyncMessage message = new LocalFileDesyncMessage(Paths.get("this/is/a/path.txt"));
		collector.onLocalFileDesynchronized(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}
	
	@Test
	public void testOnFileExecutionFailed(){
		FileExecutionFailedMessage message = new FileExecutionFailedMessage(Paths.get("this/is/a/path.txt"));
		collector.onFileExecutionFailed(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.ERROR, activityLogger);
	}
	
	@Test @Ignore
	public void testAbstractActivityCollector() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetActivityLogger() {
		fail("Not yet implemented");
	}

}

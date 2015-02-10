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
import org.peerbox.app.manager.file.FileConflictMessage;
import org.peerbox.app.manager.file.FileDeleteMessage;
import org.peerbox.app.manager.file.FileDesyncMessage;
import org.peerbox.app.manager.file.FileDownloadMessage;
import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.app.manager.file.FileUploadMessage;

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
		FileUploadMessage message = new FileUploadMessage(Paths.get("this/is/a/path.txt"));
		collector.onFileUploaded(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDownloaded() {
		FileDownloadMessage message = new FileDownloadMessage(Paths.get("this/is/a/path.txt"));
		collector.onFileDownloaded(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileDeleted() {
		FileDeleteMessage message = new FileDeleteMessage(Paths.get("this/is/a/path.txt"));
		collector.onFileDeleted(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.INFORMATION, activityLogger);
	}

	@Test
	public void testOnFileConfilct() {
		FileConflictMessage message = new FileConflictMessage(Paths.get("this/is/a/path.txt"));
		collector.onFileConfilct(message);
		
		CollectorTestUtils.captureAddActivityItem(ActivityType.WARNING, activityLogger);
	}
	
	@Test
	public void testOnFileDesynchronized(){
		FileDesyncMessage message = new FileDesyncMessage(Paths.get("this/is/a/path.txt"));
		collector.onFileDesynchronized(message);
		
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

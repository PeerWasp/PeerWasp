package org.peerbox.watchservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.watchservice.filetree.FileTreeTestSuite;
import org.peerbox.watchservice.integration.FileIntegrationTestSuite;


@RunWith(Suite.class)
@SuiteClasses({
	ActionQueueTest.class,
	FileEventManagerTest.class,
	FolderWatchServiceTest.class,
	NativeFolderWatchServiceTest.class,
	PathUtilsTest.class,

	FileTreeTestSuite.class,
	FileIntegrationTestSuite.class

})

public class WatchServiceTestSuite {

}

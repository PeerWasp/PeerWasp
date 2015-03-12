package org.peerbox.watchservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ ActionQueueTest.class, 
				FileEventManagerTest.class, 
				FolderWatchServiceTest.class,
				NativeFolderWatchServiceTest.class})

public class WatchServiceTestSuite {

}

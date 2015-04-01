package org.peerbox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.app.AppTestSuite;
import org.peerbox.events.MessageBusTest;
import org.peerbox.filerecovery.FileVersionSelectorTest;
import org.peerbox.forcesync.ForceSyncTestSuite;
import org.peerbox.guice.GuiceFxmlLoaderTest;
import org.peerbox.notifications.FileEventAggregatorTest;
import org.peerbox.presenter.PresenterTestSuite;
import org.peerbox.server.ServerTestSuite;
import org.peerbox.utils.UtilsTestSuite;
import org.peerbox.watchservice.WatchServiceTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
	AppTestSuite.class,
	MessageBusTest.class,
	FileVersionSelectorTest.class,
	ForceSyncTestSuite.class,
	GuiceFxmlLoaderTest.class,
	FileEventAggregatorTest.class,
	PresenterTestSuite.class,
	ServerTestSuite.class,
	UtilsTestSuite.class,
	WatchServiceTestSuite.class
})


public class TestSuite {

}

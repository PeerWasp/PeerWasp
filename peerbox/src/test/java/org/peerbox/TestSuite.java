package org.peerbox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.app.AppTestSuite;
import org.peerbox.app.config.ConfigTestSuite;
import org.peerbox.events.MessageBusTest;
import org.peerbox.guice.GuiceFxmlLoaderTest;
import org.peerbox.notifications.FileEventAggregatorTest;
import org.peerbox.presenter.NavigationServiceTest;
import org.peerbox.server.ServerTestSuite;
import org.peerbox.utils.UtilsTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
	AppTestSuite.class,
	MessageBusTest.class,
	ServerTestSuite.class,
	UtilsTestSuite.class,

	NavigationServiceTest.class,
	GuiceFxmlLoaderTest.class,
	FileEventAggregatorTest.class,

	ConfigTestSuite.class,



})


public class TestSuite {

}

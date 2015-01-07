package org.peerbox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.guice.GuiceFxmlLoaderTest;
import org.peerbox.h2h.FileAgentTest;
import org.peerbox.model.NodeManagerTest;
import org.peerbox.model.UserManagerTest;
import org.peerbox.notifications.FileEventAggregatorTest;
import org.peerbox.presenter.CreateNetworkControllerTest;
import org.peerbox.presenter.NavigationServiceTest;
import org.peerbox.server.ServerTestSuite;
import org.peerbox.utils.UtilsTestSuite;

@RunWith(Suite.class)
@SuiteClasses({ 
	NodeManagerTest.class,
	UserManagerTest.class,
	
	NavigationServiceTest.class,
	GuiceFxmlLoaderTest.class,
	FileAgentTest.class,
	UserConfigTest.class,
	CreateNetworkControllerTest.class,
	FileEventAggregatorTest.class,
	
	ServerTestSuite.class,
	UtilsTestSuite.class
})


public class TestSuite {

}

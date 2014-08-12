package org.peerbox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.guice.GuiceFxmlLoaderTest;
import org.peerbox.model.H2HManagerTest;
import org.peerbox.presenter.CreateNetworkControllerTest;
import org.peerbox.presenter.NavigationServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	H2HManagerTest.class,
	NavigationServiceTest.class,
	GuiceFxmlLoaderTest.class,
	CreateNetworkControllerTest.class 
	})


public class TestSuite {

}

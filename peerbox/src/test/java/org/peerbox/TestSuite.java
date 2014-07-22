package org.peerbox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.model.H2HManagerTest;
import org.peerbox.presenter.CreateNetworkControllerTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	H2HManagerTest.class, 
	CreateNetworkControllerTest.class 
	})
public class TestSuite {

}

package org.peerbox.app.config;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	UserConfigTest.class,
	AppConfigTest.class,
	BootstrappingNodesTest.class,
	BootstrappingNodesFactoryTest.class
})

public class ConfigTestSuite {

}

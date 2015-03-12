package org.peerbox.app.config;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AppConfigTest.class,
	BootstrappingNodesFactoryTest.class,
	BootstrappingNodesTest.class,
	UserConfigTest.class
})

public class ConfigTestSuite {

}

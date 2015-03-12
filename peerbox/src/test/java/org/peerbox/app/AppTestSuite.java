package org.peerbox.app;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.app.activity.ActivityTestSuite;
import org.peerbox.app.config.ConfigTestSuite;
import org.peerbox.app.manager.ManagerTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
	ActivityTestSuite.class,
	ConfigTestSuite.class,
	ManagerTestSuite.class
})
public class AppTestSuite {

}

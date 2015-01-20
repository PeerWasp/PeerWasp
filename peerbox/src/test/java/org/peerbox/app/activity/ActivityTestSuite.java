package org.peerbox.app.activity;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ActivityControllerTest.class,
	ActivityItemCellTest.class,
	ActivityItemTest.class,
	ActivityLoggerTest.class,
	ActivityTypeTest.class
})
public class ActivityTestSuite {

}

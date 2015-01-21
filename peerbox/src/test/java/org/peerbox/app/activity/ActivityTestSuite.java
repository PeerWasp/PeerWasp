package org.peerbox.app.activity;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.app.activity.collectors.CollectorsTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
	ActivityLoggerTest.class,
	ActivityControllerTest.class,
	ActivityItemTest.class,
	ActivityTypeTest.class,
	ActivityItemCellTest.class,

	CollectorsTestSuite.class
})
public class ActivityTestSuite {

}

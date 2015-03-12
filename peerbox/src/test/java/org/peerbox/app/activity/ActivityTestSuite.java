package org.peerbox.app.activity;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.app.activity.collectors.CollectorsTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
	ActivityControllerTest.class,
	ActivityItemCellTest.class,
	ActivityItemTest.class,
	ActivityLoggerTest.class,
	ActivityTypeTest.class,

	CollectorsTestSuite.class
})
public class ActivityTestSuite {

}

package org.peerbox.app.activity.collectors;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ActivityConfigurationTest.class,
	GeneralMessageCollectorTest.class,
	NodeManagerCollectorTest.class,
	UserManagerCollectorTest.class,
	FileManagerCollectorTest.class
})
public class CollectorsTestSuite {

}

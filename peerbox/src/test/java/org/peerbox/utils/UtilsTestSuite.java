package org.peerbox.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	WinRegistryTest.class,
	OsUtilsTest.class,
	NetUtilsTest.class,
	ExecuteProcessUtilsTest.class,
	AppDataTest.class
})
public class UtilsTestSuite {

}

package org.peerbox.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DialogUtilsTest.class,
	AppDataTest.class,
	ExecuteProcessUtilsTest.class,
	IconUtilsTest.class,
	NetUtilsTest.class,
	OsUtilsTest.class,
	UserConfigUtilsTest.class,
	UserDbUtilsTest.class,
	WinRegistryTest.class
})
public class UtilsTestSuite {

}

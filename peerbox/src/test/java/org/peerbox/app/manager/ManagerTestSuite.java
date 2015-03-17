package org.peerbox.app.manager;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.app.manager.file.FileInfoTest;
import org.peerbox.app.manager.file.FileManagerTest;
import org.peerbox.app.manager.node.NodeManagerTest;
import org.peerbox.app.manager.user.FileAgentTest;
import org.peerbox.app.manager.user.UserManagerTest;

@RunWith(Suite.class)
@SuiteClasses({
	// File package
	FileInfoTest.class,
	FileManagerTest.class,

	// Node package
	NodeManagerTest.class,

	// User package
	FileAgentTest.class,
	UserManagerTest.class,

	// manager package
	ProcessHandleTest.class
})
public class ManagerTestSuite {

}

package org.peerbox.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.server.servlets.FileDeleteServletTest;
import org.peerbox.server.servlets.FileRecoveryServletTest;
import org.peerbox.server.servlets.ShareFolderServletTest;
import org.peerbox.server.utils.PathDeserializerTest;
import org.peerbox.server.utils.PathSerializerTest;

@RunWith(Suite.class)
@SuiteClasses({
	// server related
	HttpServerTest.class,
	ServerFactoryTest.class,

	// servlets
	FileDeleteServletTest.class,
	FileRecoveryServletTest.class,
	ShareFolderServletTest.class,

	// utils
	PathDeserializerTest.class,
	PathSerializerTest.class
})
public class ServerTestSuite {

}

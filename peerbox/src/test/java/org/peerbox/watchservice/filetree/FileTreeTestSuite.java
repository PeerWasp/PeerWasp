package org.peerbox.watchservice.filetree;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	FileComponentTest.class,
 	FileLeafTest.class,
 	FolderCompositeTest.class,
 	RemoteFileDaoTest.class,
 })

public class FileTreeTestSuite {

}

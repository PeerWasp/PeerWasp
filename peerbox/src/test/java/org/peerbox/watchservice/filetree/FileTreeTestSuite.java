package org.peerbox.watchservice.filetree;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
 	FileLeafTest.class,
 	FolderCompositeTest.class,
 	FileComponentTest.class,
 	//PathUtilsTest.class,
 })
public class FileTreeTestSuite {

}

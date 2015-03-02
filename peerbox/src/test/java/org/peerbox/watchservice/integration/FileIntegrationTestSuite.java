package org.peerbox.watchservice.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;

@RunWith(Suite.class)
@SuiteClasses({ 
	AddDelete.class,
	Update.class,
	Rename.class,
	Move.class,
	Recover.class,
	SelectiveSynchronization.class,
	ConflictTest.class
})

public class FileIntegrationTestSuite {

}

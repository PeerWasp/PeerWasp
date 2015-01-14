package org.peerbox.watchservice.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AddDelete.class, 
				Move.class, 
				Update.class,
				Recover.class,
				SelectiveSynchronization.class })

public class IntegrationTestSuite {

}

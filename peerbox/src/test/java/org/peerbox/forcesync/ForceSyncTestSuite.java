package org.peerbox.forcesync;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ 
	RemoteAddLocalAdd.class,
	RemoteAddLocalDelete.class,
	RemoteAddLocalExists.class,
	RemoteAddLocalUnknown.class,
	RemoteDeleteLocalAdd.class,
	RemoteDeleteLocalDelete.class,
	RemoteDeleteLocalExists.class,
	RemoteDeleteLocalUnknown.class,
	RemoteExistsLocalAdd.class,
	RemoteExistsLocalDelete.class,
	RemoteExistsLocalExists.class,
	RemoteExistsLocalUnknown.class,
	RemoteUnknownLocalAdd.class,
	RemoteUnknownLocalDelete.class,
	RemoteUnknownLocalExists.class
})

public class ForceSyncTestSuite {

}

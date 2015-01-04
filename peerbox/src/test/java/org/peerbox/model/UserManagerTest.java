package org.peerbox.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.ResultStatus;
import org.peerbox.app.manager.IH2HManager;
import org.peerbox.app.manager.IUserManager;
import org.peerbox.app.manager.UserManager;

public class UserManagerTest {
	private static final int networkSize = 6;
	private static List<IH2HNode> network;

	private static IH2HNode client;
	private static UserCredentials userCredentials;
	
	private static Path root;
	
	private IUserManager userManager;
	
	@BeforeClass
	public static void beforeClass() {
		network = NetworkTestUtil.createH2HNetwork(networkSize);
	}
	
	@AfterClass
	public static void afterClass() {
		NetworkTestUtil.shutdownH2HNetwork(network);
	}
	
	@Before
	public void beforeTest() {
		userCredentials = H2HJUnitTest.generateRandomCredentials();
		root = FileTestUtil.getTempDirectory().toPath();
		
		client = network.get(RandomUtils.nextInt(0, network.size()));
		IH2HManager manager = Mockito.mock(IH2HManager.class);
		Mockito.stub(manager.getNode()).toReturn(client);
		userManager = new UserManager(manager);
	}
	
	@Test
	public void testRegister() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, ProcessExecutionException {
		// check registered
		for(int i = 0; i < network.size(); ++i) {
			IH2HManager manager = Mockito.mock(IH2HManager.class);
			Mockito.stub(manager.getNode()).toReturn(network.get(i));
			IUserManager usrMgr = new UserManager(manager);
			assertFalse(usrMgr.isRegistered(userCredentials.getUserId()));
		}
		
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		
		// check again whether registered
		for(int i = 0; i < network.size(); ++i) {
			IH2HManager manager = Mockito.mock(IH2HManager.class);
			Mockito.stub(manager.getNode()).toReturn(network.get(i));
			IUserManager usrMgr = new UserManager(manager);
			assertTrue(usrMgr.isRegistered(userCredentials.getUserId()));
		}
	}
	
	@Test
	public void testAlreadyRegistered() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, ProcessExecutionException {
		
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		
		// check again whether registered
		for(int i = 0; i < network.size(); ++i) {
			IH2HManager manager = Mockito.mock(IH2HManager.class);
			Mockito.stub(manager.getNode()).toReturn(network.get(i));
			IUserManager usrMgr = new UserManager(manager);
			res = usrMgr.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
			assertTrue(res.isError());
		}
	}
	
	@Test
	public void testNotRegisteredLogin() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, IOException, ProcessExecutionException {
		assertFalse(userManager.isRegistered(userCredentials.getUserId()));
		
		ResultStatus res = userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root);
		assertTrue(res.isError());
		
		assertFalse(userManager.isLoggedIn());
	}
	
	@Test
	public void testLogin() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, IOException, ProcessExecutionException {
		assertFalse(userManager.isLoggedIn());
		
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		assertFalse(userManager.isLoggedIn());
		
		res = userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root);
		assertTrue(res.isOk());
		assertTrue(userManager.isLoggedIn());
	}
	
	@Test
	public void testLogout() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, NoSessionException, IOException, ProcessExecutionException {
		testLogin();
		
		assertTrue(userManager.isLoggedIn());
		ResultStatus res = userManager.logoutUser();
		assertTrue(res.isOk());
		assertFalse(userManager.isLoggedIn());
	}

}

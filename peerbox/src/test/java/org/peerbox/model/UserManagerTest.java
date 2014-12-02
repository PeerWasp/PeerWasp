package org.peerbox.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.ResultStatus;

public class UserManagerTest {
	private static final int networkSize = 6;
	private static List<IH2HNode> network;

	private static IH2HNode client;
	private static UserCredentials userCredentials;
	
	private static Path root;
	
	private UserManager userManager;
	
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
		userManager = new UserManager(client.getUserManager());
	}
	
	@Test
	public void testRegister() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		// check registered
		for(int i = 0; i < network.size(); ++i) {
			UserManager usrMgr = new UserManager(network.get(i).getUserManager());
			assertFalse(usrMgr.isRegistered(userCredentials.getUserId()));
		}
		
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		
		// check again whether registered
		for(int i = 0; i < network.size(); ++i) {
			UserManager usrMgr = new UserManager(network.get(i).getUserManager());
			assertTrue(usrMgr.isRegistered(userCredentials.getUserId()));
		}
	}
	
	@Test
	public void testAlreadyRegistered() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException {
		
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		
		// check again whether registered
		for(int i = 0; i < network.size(); ++i) {
			UserManager usrMgr = new UserManager(network.get(i).getUserManager());
			res = usrMgr.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
			assertTrue(res.isError());
		}
	}
	
	@Test
	public void testNotRegisteredLogin() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, IOException {
		assertFalse(userManager.isRegistered(userCredentials.getUserId()));
		
		ResultStatus res = userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root);
		assertTrue(res.isError());
		
		assertFalse(userManager.isLoggedIn(null));
	}
	
	@Test
	public void testLogin() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, IOException {
		assertFalse(userManager.isLoggedIn(null));
		
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		assertFalse(userManager.isLoggedIn(null));
		
		res = userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root);
		assertTrue(res.isOk());
		assertTrue(userManager.isLoggedIn(null));
	}
	
	@Test
	public void testLogout() throws NoPeerConnectionException, InterruptedException, InvalidProcessStateException, NoSessionException, IOException {
		testLogin();
		
		assertTrue(userManager.isLoggedIn(null));
		ResultStatus res = userManager.logoutUser();
		assertTrue(res.isOk());
		assertFalse(userManager.isLoggedIn(null));
	}

}

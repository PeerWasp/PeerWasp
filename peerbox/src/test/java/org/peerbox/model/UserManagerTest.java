package org.peerbox.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.peerbox.IUserConfig;
import org.peerbox.ResultStatus;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.app.manager.user.LoginMessage;
import org.peerbox.app.manager.user.LogoutMessage;
import org.peerbox.app.manager.user.RegisterMessage;
import org.peerbox.app.manager.user.UserManager;
import org.peerbox.events.MessageBus;

public class UserManagerTest {
	private static final int networkSize = 6;
	private static List<IH2HNode> network;

	private UserCredentials userCredentials;
	private Path root;
	private ClientContext client;
	
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
		root = FileTestUtil.getTempDirectory().toPath();
		userCredentials = H2HJUnitTest.generateRandomCredentials();

		// assemble random user manager to test
		IH2HNode clientNode = network.get(RandomUtils.nextInt(0, network.size()));
		client = createClientContext(clientNode);
	}
	
	@Test
	public void testRegister() throws NoPeerConnectionException {
		// check NOT registered
		for(int i = 0; i < network.size(); ++i) {
			ClientContext cc = createClientContext(network.get(i));
			IUserManager usrMgr = cc.getUserManager();
			assertFalse(usrMgr.isRegistered(userCredentials.getUserId()));
		}
		
		// register user
		ResultStatus res = client.getUserManager().registerUser(
				userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		
		// got event?
		ArgumentCaptor<RegisterMessage> event = ArgumentCaptor.forClass(RegisterMessage.class);
		Mockito.verify(client.getMessageBus(), Mockito.times(1)).publish(event.capture());
		assertNotNull(event.getValue());
		assertEquals(userCredentials.getUserId(), event.getValue().getUsername());
		
		// check again whether registered
		for(int i = 0; i < network.size(); ++i) {
			ClientContext cc = createClientContext(network.get(i));
			IUserManager usrMgr = cc.getUserManager();
			assertTrue(usrMgr.isRegistered(userCredentials.getUserId()));
		}
	}
	
	@Test
	public void testAlreadyRegistered() throws NoPeerConnectionException {

		// register
		ResultStatus res = client.getUserManager().registerUser(
				userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		
		// got event?
		Mockito.verify(client.getMessageBus(), Mockito.times(1)).publish(Mockito.anyObject());
		
		// try register -- should fail
		for (int i = 0; i < network.size(); ++i) {
			ClientContext cc = createClientContext(network.get(i));
			IUserManager usrMgr = cc.getUserManager();
			res = usrMgr.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
			assertTrue(res.isError());
			
			// got NO event?
			Mockito.verify(cc.getMessageBus(), Mockito.never()).publish(Mockito.anyObject());
		}
	}
	
	@Test
	public void testLogin() throws NoPeerConnectionException, IOException {
		IUserManager userManager = client.getUserManager();
		// not loggedIn
		assertFalse(userManager.isLoggedIn());
		
		// register
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		assertFalse(userManager.isLoggedIn());
		// got event?
		Mockito.verify(client.getMessageBus(), Mockito.times(1)).publish(Mockito.any());
		
		// login and loggedIn
		res = userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root);
		assertTrue(res.isOk());
		assertTrue(userManager.isLoggedIn());
		
		// got event?
		ArgumentCaptor<LoginMessage> event = ArgumentCaptor.forClass(LoginMessage.class);
		Mockito.verify(client.getMessageBus(), Mockito.times(2)).publish(event.capture());
		assertNotNull(event.getValue());
		assertEquals(userCredentials.getUserId(), event.getValue().getUsername());
	}

	@Test
	public void testNotRegisteredLogin() throws NoPeerConnectionException, IOException {
		IUserManager userManager = client.getUserManager();
		
		// not registered
		assertFalse(userManager.isRegistered(userCredentials.getUserId()));
		
		// login and not loggedIn
		ResultStatus res = userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root);
		assertTrue(res.isError());
		assertFalse(userManager.isLoggedIn());
		
		// got NO event?
		Mockito.verify(client.getMessageBus(), Mockito.never()).publish(Mockito.anyObject());
	}
	
	@Test
	public void testLoginWrongCredentials() throws NoPeerConnectionException, IOException {
		IUserManager userManager = client.getUserManager();
		// not loggedIn
		assertFalse(userManager.isLoggedIn());
		
		// register
		ResultStatus res = userManager.registerUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin());
		assertTrue(res.isOk());
		assertFalse(userManager.isLoggedIn());
		// got event?
		Mockito.verify(client.getMessageBus(), Mockito.times(1)).publish(Mockito.any());
		
		// wrong credentials -- login and loggedIn
		UserCredentials wrongCred = H2HJUnitTest.generateRandomCredentials();
		res = userManager.loginUser(wrongCred.getUserId(), wrongCred.getPassword(), wrongCred.getPin(), root);
		assertFalse(res.isOk());
		assertFalse(userManager.isLoggedIn());
		// got NO additional event?
		Mockito.verify(client.getMessageBus(), Mockito.times(1)).publish(Mockito.any());
				
		// correct credentials -- login and loggedIn
		res = userManager.loginUser(userCredentials.getUserId(), userCredentials.getPassword(), userCredentials.getPin(), root);
		assertTrue(res.isOk());
		assertTrue(userManager.isLoggedIn());
		// got event?
		ArgumentCaptor<LoginMessage> event = ArgumentCaptor.forClass(LoginMessage.class);
		Mockito.verify(client.getMessageBus(), Mockito.times(2)).publish(event.capture());
		assertNotNull(event.getValue());
		assertEquals(userCredentials.getUserId(), event.getValue().getUsername());
	}
	
	@Test
	public void testLogout() throws NoPeerConnectionException, IOException, NoSessionException {
		// register and login
		testLogin();
		IUserManager userManager = client.getUserManager();
		
		assertTrue(userManager.isLoggedIn());
		ResultStatus res = userManager.logoutUser();
		assertTrue(res.isOk());
		assertFalse(userManager.isLoggedIn());
		
		// got event? (Register, login, logout)
		ArgumentCaptor<LogoutMessage> event = ArgumentCaptor.forClass(LogoutMessage.class);
		Mockito.verify(client.getMessageBus(), Mockito.times(3)).publish(event.capture());
		assertNotNull(event.getValue());
		assertEquals(userCredentials.getUserId(), event.getValue().getUsername());
	}
	
	@Test(expected=NoSessionException.class)
	public void testLogoutNotLoggedIn() throws NoPeerConnectionException, NoSessionException {
		IUserManager userManager = client.getUserManager();
		assertFalse(userManager.isLoggedIn());
		
		ResultStatus res = userManager.logoutUser();
		assertFalse(res.isOk());
		fail("NoSessionException not thrown.");
	}
	
	private ClientContext createClientContext(IH2HNode node) {
		return new ClientContext(node);
	}
	
	private class ClientContext {
		private IH2HNode node;
		private INodeManager nodeManager;
		private IUserManager userManager;
		private MessageBus messageBus;

		public ClientContext(IH2HNode node) {
			this.node = node;

			nodeManager = Mockito.mock(INodeManager.class);
			Mockito.stub(nodeManager.getNode()).toReturn(this.node);
			messageBus = Mockito.mock(MessageBus.class);
			IUserConfig userConfig = Mockito.mock(IUserConfig.class);
			userManager = new UserManager(nodeManager, userConfig, messageBus);
		}

		public IUserManager getUserManager() {
			return userManager;
		}

		public MessageBus getMessageBus() {
			return messageBus;
		}
	}
}

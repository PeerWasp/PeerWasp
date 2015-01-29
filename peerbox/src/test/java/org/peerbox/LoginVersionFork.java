package org.peerbox;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoginVersionFork extends BaseJUnitTest {

	private static final Logger LOG = LoggerFactory.getLogger(LoginVersionFork.class);

	private UserCredentials credentials = new UserCredentials("username", "password!", "pin123");

	@Test
	public void testLogin() throws UnknownHostException {
		boolean success = false;
		// initial node
		INetworkConfiguration netConfigInitial = NetworkConfiguration.createInitial();
		IH2HNode initialNode = H2HNode.createNode(FileConfiguration.createDefault());
		success = initialNode.connect(netConfigInitial);
		assertTrue(success);

		// register and login
		try {

			initialNode.getUserManager()
				.createRegisterProcess(credentials).execute();
			initialNode.getUserManager()
				.createLoginProcess(credentials, new TestFileAgent()).execute();

		} catch (InvalidProcessStateException | ProcessExecutionException
				| NoPeerConnectionException e) {
			LOG.warn("Exception: {}", e.getMessage(), e);
			fail("Exception: " + e.getMessage());
		}

		// connect 2nd node to initial node on localhost
		InetAddress address = InetAddress.getByName("localhost");
		INetworkConfiguration netConfig = NetworkConfiguration.create(UUID.randomUUID().toString(),  address);
		IH2HNode peerNode = H2HNode.createNode(FileConfiguration.createDefault());
		success = peerNode.connect(netConfig);
		assertTrue(success);

		// login

		// register and login
		try {

			initialNode.getUserManager()
				.createLoginProcess(credentials, new TestFileAgent()).execute();

		} catch (InvalidProcessStateException | ProcessExecutionException
				| NoPeerConnectionException e) {
			e.printStackTrace();
			LOG.warn("Exception: {}", e.getMessage(), e);
			fail("Exception: " + e.getMessage());
		}

	}
}

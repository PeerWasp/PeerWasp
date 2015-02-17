package org.peerbox.app.manager.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.node.NodeConnectMessage;
import org.peerbox.app.manager.node.NodeDisconnectMessage;
import org.peerbox.app.manager.node.NodeManager;
import org.peerbox.events.MessageBus;

public class NodeManagerTest {

	private INodeManager nodeManager;
	private MessageBus messageBus;

	// used if node joins to a network
	private INodeManager initialNodeManager;


	@Before
	public void initialize() {
		messageBus = Mockito.mock(MessageBus.class);
		nodeManager = new NodeManager(messageBus);
	}

	@After
	public void cleanup() {
		if (initialNodeManager != null) {
			initialNodeManager.leaveNetwork();
			initialNodeManager = null;
		}

		if (nodeManager != null && nodeManager.isConnected()) {
			nodeManager.leaveNetwork();
			nodeManager = null;
		}
	}

	@Test
	public void testGetNode_Initial() {
		assertNull(nodeManager.getNode());

		// create
		boolean ret = nodeManager.createNetwork();
		assertTrue(ret);
		assertNotNull(nodeManager.getNode());

		// leave
		ret = nodeManager.leaveNetwork();
		assertTrue(ret);
		assertNull(nodeManager.getNode());
	}

	@Test
	public void testGetNode_Join() throws UnknownHostException {
		initialNodeManager = createInitialNode();

		// join
		assertNull(nodeManager.getNode());
		boolean ret = nodeManager.joinNetwork("localhost");
		assertTrue(ret);
		assertNotNull(nodeManager.getNode());

		// leave
		ret = nodeManager.leaveNetwork();
		assertTrue(ret);
		assertNull(nodeManager.getNode());
	}

	@Test
	public void testCreateNetwork() {
		boolean ret = nodeManager.createNetwork();
		assertTrue(ret);
		assertTrue(nodeManager.getNetworkConfiguration().isInitial());

		// got event?
		ArgumentCaptor<NodeConnectMessage> event = ArgumentCaptor.forClass(NodeConnectMessage.class);
		// 1. NodeConnect
		Mockito.verify(messageBus, Mockito.times(1)).publish(event.capture());
		assertNotNull(event.getValue());
		assertTrue(event.getValue() instanceof NodeConnectMessage);
		assertEquals(event.getValue().getNodeAddress(), "localhost");
	}

	@Test
	public void testJoinNetwork_Node() throws UnknownHostException {
		initialNodeManager = createInitialNode();

		boolean ret = nodeManager.joinNetwork("127.0.0.1");
		assertTrue(ret);
		assertFalse(nodeManager.getNetworkConfiguration().isInitial());
		assertEquals(nodeManager.getNetworkConfiguration().getBootstrapAddress().getHostAddress(), "127.0.0.1");

		// got event?
		ArgumentCaptor<NodeConnectMessage> event = ArgumentCaptor.forClass(NodeConnectMessage.class);
		// 1. NodeConnect
		Mockito.verify(messageBus, Mockito.times(1)).publish(event.capture());
		assertNotNull(event.getValue());
		assertTrue(event.getValue() instanceof NodeConnectMessage);
		assertEquals(event.getValue().getNodeAddress(), "127.0.0.1");
	}

	@Test
	public void testJoinNetwork_Node_WrongAddress() throws UnknownHostException {
		initialNodeManager = createInitialNode();

		boolean ret = nodeManager.joinNetwork("1.2.3.4");
		assertFalse(ret);
		// got NO event
		Mockito.verify(messageBus, Mockito.never()).publish(Mockito.anyObject());
	}

	@Test
	public void testJoinNetwork_Node_UnknownHost() {
		initialNodeManager = createInitialNode();
		try {
			nodeManager.joinNetwork("unknownHost");
			fail("Exception was not thrown, but was expected!");
		} catch(UnknownHostException uhe) {
			// ignore -- exception is expected
		}

		// got NO event
		Mockito.verify(messageBus, Mockito.never()).publish(Mockito.anyObject());
	}

	@Test
	public void testJoinNetwork_NodeList() {
		List<String> addresses = new ArrayList<>();
		addresses.add("1.2.3.4");		// should fail
		addresses.add("unknownHost");	// should fail
		addresses.add("127.0.0.1");		// should succeed

		initialNodeManager = createInitialNode();

		// should connect to last host and other two should not lead to an exception
		boolean ret = nodeManager.joinNetwork(addresses);
		assertTrue(ret);
		assertFalse(nodeManager.getNetworkConfiguration().isInitial());
		assertEquals(nodeManager.getNetworkConfiguration().getBootstrapAddress().getHostAddress(), "127.0.0.1");

		// got event?
		ArgumentCaptor<NodeConnectMessage> event = ArgumentCaptor.forClass(NodeConnectMessage.class);
		// 1. NodeConnect
		Mockito.verify(messageBus, Mockito.times(1)).publish(event.capture());
		assertNotNull(event.getValue());
		assertTrue(event.getValue() instanceof NodeConnectMessage);
		assertEquals(event.getValue().getNodeAddress(), "127.0.0.1");
	}


	@Test(expected = UnknownHostException.class)
	public void testJoinNetwork_UnknownHost() throws UnknownHostException {
		nodeManager.joinNetwork("unknownhost");
	}

	@Test
	public void testJoinNetwork_WrongAddress() throws UnknownHostException{
		assertFalse(nodeManager.joinNetwork("1.2.3.4"));
	}

	@Test
	public void testJoinNetwork_CorrectAddress() throws UnknownHostException {
		assertTrue(nodeManager.joinNetwork("localhost"));
	}

	@Test
	public void testLeaveNetwork_Join() throws UnknownHostException {
		initialNodeManager = createInitialNode();

		boolean ret = nodeManager.joinNetwork("localhost");
		assertTrue(ret);

		nodeManager.leaveNetwork();

		// got event?
		ArgumentCaptor<NodeDisconnectMessage> event = ArgumentCaptor.forClass(NodeDisconnectMessage.class);
		// 1. NodeConnect, 2. NodeDisconnect
		Mockito.verify(messageBus, Mockito.times(2)).publish(event.capture());
		assertNotNull(event.getValue());
		assertTrue(event.getValue() instanceof NodeDisconnectMessage);
	}

	@Test
	public void testLeaveNetwork_Initial() throws UnknownHostException {
		boolean ret = nodeManager.createNetwork();
		assertTrue(ret);

		nodeManager.leaveNetwork();

		// got event?
		ArgumentCaptor<NodeDisconnectMessage> event = ArgumentCaptor.forClass(NodeDisconnectMessage.class);
		// 1. NodeConnect, 2. NodeDisconnect
		Mockito.verify(messageBus, Mockito.times(2)).publish(event.capture());
		assertNotNull(event.getValue());
		assertTrue(event.getValue() instanceof NodeDisconnectMessage);
	}

	@Test
	public void testLeaveNetwork_NotConnected() {
		boolean ret = nodeManager.leaveNetwork();
		assertTrue(ret);

		// got NO event?
		Mockito.verify(messageBus, Mockito.never()).publish(Mockito.anyObject());
	}

	@Test
	public void testIsConnected_Initial() {
		assertFalse(nodeManager.isConnected());

		boolean ret = nodeManager.createNetwork();
		assertTrue(ret);
		assertTrue(nodeManager.isConnected());

		ret = nodeManager.leaveNetwork();
		assertTrue(ret);
		assertFalse(nodeManager.isConnected());
	}

	@Test
	public void testIsConnected_Join() throws UnknownHostException {
		initialNodeManager = createInitialNode();

		assertFalse(nodeManager.isConnected());

		boolean ret = nodeManager.joinNetwork("localhost");
		assertTrue(ret);
		assertTrue(nodeManager.isConnected());

		ret = nodeManager.leaveNetwork();
		assertTrue(ret);
		assertFalse(nodeManager.isConnected());
	}

	@Test
	public void testInetAddressIfNotInitialPeer() throws UnknownHostException {
		String bootstrapAddress = "1.2.3.4";
		nodeManager.joinNetwork(bootstrapAddress);
		String address = nodeManager.getNetworkConfiguration().getBootstrapAddress().toString();
		assertEquals(address, InetAddress.getByName(bootstrapAddress).toString());
	}

	@Test
	public void testGetNetworkConfiguration_Initial() {
		assertNull(nodeManager.getNetworkConfiguration());
		boolean ret = nodeManager.createNetwork();
		assertTrue(ret);
		assertNotNull(nodeManager.getNetworkConfiguration());
	}

	@Test
	public void testGetNetworkConfiguration_Join() throws UnknownHostException {
		initialNodeManager = createInitialNode();

		assertNull(nodeManager.getNetworkConfiguration());
		boolean ret = nodeManager.joinNetwork("127.0.0.1");
		assertTrue(ret);
		assertNotNull(nodeManager.getNetworkConfiguration());
	}

	@Test
	public void testGetFileConfiguration_Initial() {
		assertNull(nodeManager.getFileConfiguration());
		boolean ret = nodeManager.createNetwork();
		assertTrue(ret);
		assertNotNull(nodeManager.getFileConfiguration());
	}

	@Test
	public void testGetFileConfiguration_Join() throws UnknownHostException {
		initialNodeManager = createInitialNode();

		assertNull(nodeManager.getFileConfiguration());
		boolean ret = nodeManager.joinNetwork("127.0.0.1");
		assertTrue(ret);
		assertNotNull(nodeManager.getFileConfiguration());
	}

	private INodeManager createInitialNode() {
		INodeManager init = new NodeManager(Mockito.mock(MessageBus.class));
		assertNotNull(init);
		boolean ret = init.createNetwork();
		assertTrue(ret);
		return init;
	}

}

package org.peerbox.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.engio.mbassy.listener.Handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageBusTest {

	private MessageBus messageBus;

	@Before
	public void setUp() throws Exception {
		messageBus = new MessageBus();
	}

	@After
	public void tearDown() throws Exception {
		if (messageBus != null) {
			messageBus.shutdown();
			messageBus = null;
		}
	}

	@Test
	public void testMessageBus() {
		TestListener listener = new TestListener();
		messageBus.subscribe(listener);

		assertFalse(listener.messageReceived);
		messageBus.publish(new TestMessage());
		assertTrue(listener.messageReceived);
	}

	private class TestMessage implements IMessage {

	}

	private class TestListener {
		boolean messageReceived = false;

		@Handler
		private void testMessageHandler(TestMessage msg) {
			messageReceived = true;
		}
	}

}

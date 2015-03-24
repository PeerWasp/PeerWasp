package org.peerbox.events;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;

import com.google.inject.Singleton;

/**
 * The message bus enables event delivery following the publish-subscribe pattern.
 *
 * - Messages:
 * Messages must implement the interface {@link IMessage}.
 *
 * - Subscribers:
 * Subscribers must be registered in advance by calling {@link #subscribe(Object)}.
 * Furthermore, subscribers should implement handler methods accepting a specific message type
 * as parameter. Handlers must be annotated with @Handler ({@link net.engio.mbassy.listener.Handler}.
 *
 * - Publishers:
 * Publishers can publish messages implementing {@link IMessage} by
 * calling {@link #publish(IMessage)} (synchronous delivery).
 *
 * @author albrecht
 *
 */
@Singleton
public class MessageBus extends MBassador<IMessage> {

	public MessageBus() {
		super(createBusConfiguration());
	}

	private static BusConfiguration createBusConfiguration() {
		BusConfiguration config = new BusConfiguration();
		// synchronous dispatching of events
		config.addFeature(Feature.SyncPubSub.Default());
		// asynchronous dispatching of events
		config.addFeature(Feature.AsynchronousHandlerInvocation.Default());
		config.addFeature(Feature.AsynchronousMessageDispatch.Default());
		return config;
	}
}

package org.peerbox.app;

import org.peerbox.app.activity.collectors.ActivityConfiguration;
import org.peerbox.app.config.AppConfig;
import org.peerbox.events.MessageBus;
import org.peerbox.server.IServer;
import org.peerbox.view.UiContext;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * The app context contains application-wide ("global") services and dependencies such
 * as the message bus or the HTTP server.
 *
 * @author albrecht
 *
 */
@Singleton
public class AppContext {

	private Injector injector;

	private MessageBus messageBus;

	private AppConfig appConfig;

	private IServer server;

	private ActivityConfiguration activityConfiguration;

	private UiContext uiContext;

	private ClientContext currentClientContext;

	public Injector getInjector() {
		return injector;
	}

	@Inject
	protected void setInjector(Injector injector) {
		this.injector = injector;
	}

	public MessageBus getMessageBus() {
		return messageBus;
	}

	@Inject
	protected void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	public AppConfig getAppConfig() {
		return appConfig;
	}

	@Inject
	protected void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public IServer getServer() {
		return server;
	}

	@Inject
	protected void setServer(IServer server) {
		this.server = server;
	}

	public ActivityConfiguration getActivityConfiguration() {
		return activityConfiguration;
	}

	@Inject
	protected void setActivityConfiguration(ActivityConfiguration activityConfiguration) {
		this.activityConfiguration = activityConfiguration;
	}

	public UiContext getUiContext() {
		return uiContext;
	}

	@Inject
	protected void setUiContext(UiContext uiContext) {
		this.uiContext = uiContext;
	}

	public ClientContext getCurrentClientContext() {
		return currentClientContext;
	}

	public void setCurrentClientContext(ClientContext currentClientContext) {
		this.currentClientContext = currentClientContext;
	}

}

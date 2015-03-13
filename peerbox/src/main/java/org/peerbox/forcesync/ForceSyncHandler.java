package org.peerbox.forcesync;

import java.nio.file.Path;

import org.peerbox.app.AppContext;
import org.peerbox.view.tray.SynchronizationCompleteNotification;
import org.peerbox.view.tray.SynchronizationStartsNotification;

import com.google.inject.Inject;

public class ForceSyncHandler implements IForceSyncHandler{

	private AppContext appContext;
	
	@Inject
	public ForceSyncHandler(AppContext appContext) {
		this.appContext = appContext;
	}
	
	@Override
	public void forceSync(Path topLevel) {
		appContext.getMessageBus().publish(new ForceSyncMessage(topLevel));
		
		ForceSync forceSync = new ForceSync(appContext.getCurrentClientContext());
		appContext.getMessageBus().publish(new SynchronizationStartsNotification());
		forceSync.forceSync(topLevel);
		appContext.getMessageBus().publish(new SynchronizationCompleteNotification());
	}

	@Override
	public void forceSync() {
		forceSync(appContext.getCurrentClientContext().getUserConfig().getRootPath());
	}

}

package org.peerbox.forcesync;

import java.nio.file.Path;

import org.peerbox.app.AppContext;

import com.google.inject.Inject;

public class ForceSyncHandler implements IForceSyncHandler{

	private Path topLevel;
	
	private AppContext appContext;
	
	@Inject
	public ForceSyncHandler(AppContext appContext) {
		this.appContext = appContext;
	}
	
	@Override
	public void forceSync(Path topLevel) {
		this.topLevel = topLevel;
		appContext.getMessageBus().publish(new ForceSyncMessage(topLevel));
		
		ForceSync forceSync = new ForceSync(appContext.getCurrentClientContext());
		forceSync.startForceSync(topLevel);
		appContext.getMessageBus().publish(new ForceSyncCompleteMessage());
	}

	@Override
	public void forceSync() {
		forceSync(appContext.getCurrentClientContext().getUserConfig().getRootPath());
	}

}

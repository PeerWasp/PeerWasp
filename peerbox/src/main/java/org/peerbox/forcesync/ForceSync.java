package org.peerbox.forcesync;

import java.nio.file.Path;
import java.util.Set;

import org.peerbox.app.ClientContext;
import org.peerbox.watchservice.filetree.FileTreeInitializer;
import org.peerbox.watchservice.filetree.persistency.ListSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForceSync {

	private static final Logger logger = LoggerFactory.getLogger(ForceSync.class); 
	private ClientContext context;

	public ForceSync(ClientContext currentClientContext) {
		this.context = currentClientContext;
	}

	public void startForceSync(Path topLevel) {
		try {
			logger.trace("Start forced synchronization on {}", topLevel);
			FileTreeInitializer fileTreeInitializer = new FileTreeInitializer(context);
			fileTreeInitializer.initialize(topLevel);
			Set<Path> pendingEvents = context.getFileEventManager().getPendingEvents();
			if(pendingEvents.size() > 0){
				logger.trace("New events happened during force sync. Redo.");
				pendingEvents.clear();
				startForceSync(topLevel);
//				Path topLevel = pendingEvents.
//				for(Path path : pendingEvents){
//					if(path.startsWith(other))
//				}
			} else {
				context.getFileEventManager().setCleanupRunning(false);
				ListSync listSync = context.getInjector().getInstance(ListSync.class);
				listSync.sync();
				context.getActionExecutor().setForceSyncRunning(false);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}

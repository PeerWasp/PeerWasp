package org.peerbox.app.activity.collectors;

import com.google.inject.Inject;

/**
 * This class acts as a dependency helper for Guice.
 * All collectors that should be created and active should have a reference here, i.e. this
 * class depends on all active collectors. Thus, all instances will be resolved and created at
 * the beginning and wired together with the ActivityLogger.
 *
 * @author albrecht
 *
 */
public final class ActivityConfiguration  {

	private GeneralMessageCollector generalMessageCollector;
	private NodeManagerCollector nodeManagerCollector;
	private UserManagerCollector userManagerCollector;
	private FileManagerCollector fileManagerCollector;

	protected ActivityConfiguration() {

	}

	public GeneralMessageCollector getGeneralMessageCollector() {
		return generalMessageCollector;
	}

	@Inject
	public void setGeneralMessageCollector(GeneralMessageCollector generalMessageCollector) {
		this.generalMessageCollector = generalMessageCollector;
	}

	public NodeManagerCollector getNodeManagerCollector() {
		return nodeManagerCollector;
	}

	@Inject
	public void setNodeManagerCollector(NodeManagerCollector nodeManagerCollector) {
		this.nodeManagerCollector = nodeManagerCollector;
	}

	public UserManagerCollector getUserManagerCollector() {
		return userManagerCollector;
	}

	@Inject
	public void setUserManagerCollector(UserManagerCollector userManagerCollector) {
		this.userManagerCollector = userManagerCollector;
	}

	public FileManagerCollector getFileManagerCollector() {
		return fileManagerCollector;
	}

	@Inject
	public void setFileManagerCollector(FileManagerCollector fileManagerCollector) {
		this.fileManagerCollector = fileManagerCollector;
	}


}

package org.peerbox.bootstrapnode;

import java.net.UnknownHostException;
import java.util.List;

import org.peerbox.model.H2HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapNode {
	
	private static final Logger logger = LoggerFactory.getLogger(BootstrapNode.class);

	private H2HManager h2hManager;
	
	public BootstrapNode() {
		h2hManager = new H2HManager();
	}
	
	public void start(List<String> bootstrapNodes) throws UnknownHostException {
		boolean success = false;
		success = h2hManager.joinNetwork(bootstrapNodes);
		
		if(!success) {
			logger.info("Create initial node (could not connect to existing nodes)");
			h2hManager.createNode();
		} else {
			logger.info("Connected to network.");
		}
	}
	
}

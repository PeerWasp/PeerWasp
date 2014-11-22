package org.peerbox.bootstrapnode;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Starter {
	
	private static final Logger logger = LoggerFactory.getLogger(Starter.class);
	private static final String BOOTSTRAP_FILENAME = "bootstrapnodes.txt";
	
	public static void main(String[] args) {
		
		try {
			
			List<String> bootstrapNodes = loadBootstrapNodes();
			new BootstrapNode().start(bootstrapNodes);
		
		} catch (UnknownHostException e) {
			logger.error("Could not start node", e);
		} catch (IOException e) {
			logger.error("Could not read bootstrap file.", e);
		}
	}

	private static List<String> loadBootstrapNodes() throws IOException {
		return FileUtils.readLines(new File(BOOTSTRAP_FILENAME));
	}
}

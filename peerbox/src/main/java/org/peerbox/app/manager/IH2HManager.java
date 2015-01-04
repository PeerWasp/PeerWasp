package org.peerbox.app.manager;

import java.net.UnknownHostException;
import java.util.List;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;


public interface IH2HManager {
	
	IH2HNode getNode();
	
	boolean joinNetwork(List<String> bootstrappingNodes);
	
	boolean joinNetwork(String address) throws UnknownHostException;
	
	boolean createNetwork();
	
	boolean leaveNetwork();
	
	boolean isConnected();

	INetworkConfiguration getNetworkConfiguration();
}

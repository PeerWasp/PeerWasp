package org.peerbox.app.manager;

import java.net.UnknownHostException;

import org.hive2hive.core.api.interfaces.IH2HNode;


public interface IH2HManager {
	
	IH2HNode getNode();
	
	boolean joinNetwork(String address) throws UnknownHostException;
	
	boolean createNetwork();
	
	boolean leaveNetwork();
	
	boolean isConnected();
	
}

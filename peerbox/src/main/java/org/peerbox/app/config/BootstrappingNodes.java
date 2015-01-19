package org.peerbox.app.config;

import java.util.LinkedHashSet;
import java.util.Set;

public class BootstrappingNodes {

	private String lastNode;
	private final Set<String> bootstrappingNodes;

	protected BootstrappingNodes() {
		this.lastNode = "";
		this.bootstrappingNodes = new LinkedHashSet<String>();
	}

	public String getLastNode() {
		return lastNode;
	}

	public void setLastNode(final String lastNode) {
		this.lastNode = lastNode != null ? lastNode.trim() : "";
	}

	public boolean hasLastNode() {
		return lastNode != null && !lastNode.trim().isEmpty();
	}

	public Set<String> getBootstrappingNodes() {
		return bootstrappingNodes;
	}

	public boolean hasBootstrappingNodes() {
		return !bootstrappingNodes.isEmpty();
	}

	public void addNode(String node) {
		if(node == null) {
			return;
		}
		node = node.trim();
		if(node.isEmpty()) {
			return;
		}
		bootstrappingNodes.add(node);
	}

	public void removeNode(String node) {
		bootstrappingNodes.remove(node);
		if (node != null) {
			bootstrappingNodes.remove(node.trim());
		}
	}

	public void clearNodes() {
		bootstrappingNodes.clear();
	}

}

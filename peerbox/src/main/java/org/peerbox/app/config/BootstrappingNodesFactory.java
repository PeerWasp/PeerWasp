package org.peerbox.app.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Factory for the bootstrapping nodes (addresses to nodes).
 * The factory is responsible for reading and writing the content of a
 * {@link BootstrappingNodes} instance from and to text files.
 *
 * @author albrecht
 *
 */
public final class BootstrappingNodesFactory {

	/* single last node that was used (connected to) */
	private Path lastNodeFile;
	/* url to resource containing default node addresses */
	private URL nodesDefaultUrl;
	/* file with nodes - user specific */
	private Path nodesFile;

	/* nodes instance */
	private BootstrappingNodes bn;

	/**
	 * Creates a new instance.
	 *
	 * @return new instance
	 */
	public BootstrappingNodes create() {
		bn = new BootstrappingNodes();
		return bn;
	}

	/**
	 * Loads stored content from files.
	 * Note: lastNodeFile, nodesFile and nodesDefaultUrl must be set!
	 *
	 * @throws IOException if reading from files fails.
	 */
	public void load() throws IOException {
		clear();

		if (lastNodeFile != null) {
			loadLastNode();
		}
		if (nodesFile != null) {
			loadNodesFromFile(nodesFile);
		}
		if (nodesDefaultUrl != null) {
			loadNodesFromUrl(nodesDefaultUrl);
		}
	}

	private void loadNodesFromUrl(final URL url) throws IOException {
		List<String> lines = Resources.readLines(url, Charsets.UTF_8);
		loadNodesFromList(lines);
	}

	private void clear() {
		bn.setLastNode("");
		bn.getBootstrappingNodes().clear();
	}

	private void loadLastNode() throws IOException {
		bn.setLastNode("");
		if (Files.exists(lastNodeFile) && Files.isRegularFile(lastNodeFile)) {
			List<String> lines = Files.readAllLines(lastNodeFile);
			if (!lines.isEmpty()) {
				bn.setLastNode(lines.get(0).trim());
			}
		}
	}

	private void loadNodesFromFile(final Path file) throws IOException {
		if (!Files.exists(file)) {
			return;
		}
		if (!Files.isRegularFile(file)) {
			return;
		}
		List<String> lines = Files.readAllLines(file);
		loadNodesFromList(lines);
	}

	private void loadNodesFromList(final List<String> lines) {
		for (String n : lines) {
			n = n.trim();
			if (!n.isEmpty()) {
				bn.getBootstrappingNodes().add(n);
			}
		}
	}

	/**
	 * Stores the content of the instance to files.
	 *
	 * @throws IOException if writing fails.
	 */
	public void save() throws IOException {
		// Note: default nodes are not changed! they are static and fixed.
		if (lastNodeFile != null) {
			saveLastNode();
		}
		if (nodesFile != null) {
			saveNodes();
		}
	}

	private void saveLastNode() throws IOException {
		Files.write(lastNodeFile, bn.getLastNode().getBytes());
	}

	private void saveNodes() throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String n : bn.getBootstrappingNodes()) {
			sb.append(n.trim()).append("\n");
		}
		Files.write(nodesFile, sb.toString().getBytes());
	}

	/**
	 * File where the last node that was used is stored.
	 *
	 * @return path
	 */
	public Path getLastNodeFile() {
		return lastNodeFile;
	}

	public void setLastNodeFile(Path lastNodeFile) {
		this.lastNodeFile = lastNodeFile;
	}

	/**
	 * Url pointing to a resource where default nodes are stored (in JAR package)
	 *
	 * @return resource url
	 */
	public URL getNodesDefaultFile() {
		return nodesDefaultUrl;
	}

	public void setNodesDefaultUrl(URL nodesDefaultUrl) {
		this.nodesDefaultUrl = nodesDefaultUrl;
	}

	/**
	 * File where a list of node addresses is stored.
	 *
	 * @return path
	 */
	public Path getNodesFile() {
		return nodesFile;
	}

	public void setNodesFile(Path nodesFile) {
		this.nodesFile = nodesFile;
	}

}

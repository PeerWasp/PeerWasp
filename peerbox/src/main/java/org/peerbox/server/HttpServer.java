package org.peerbox.server;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.servlet.GuiceFilter;

/**
 * Simple HTTP server that handles incoming web requests.
 * 
 * @author albrecht
 *
 */
public class HttpServer implements IServer {

	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private Server server;
	private int port = 0;

	/**
	 * Creates and initializes a new server with the given path, but does not start it.
	 * 
	 * @param port
	 */
	public HttpServer(int port) {
		this.port = port;
		server = new Server(port);
		
		initializeHandler();
	}

	private void initializeHandler() {
		// default servlet required for jetty to accept all requests 
		// (guice will define mappings between urls and servlets)
		ServletContextHandler handler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
		handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
		handler.addServlet(DefaultServlet.class, "/");
	}

	@Override
	public void start() {
		try {
			server.start();
			logger.info("Server started (port {})", getPort());
		} catch (Exception e) {
			logger.warn("Could not start the server.", e);
		}
	}

	@Override
	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			logger.warn("Could not stop the server.", e);
		}
	}

	@Override
	public int getPort() {
		return port;
	}
}

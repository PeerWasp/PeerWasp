package org.peerbox.server;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTTP server that handles incoming web requests.
 * 
 * @author albrecht
 *
 */
public class HttpServer implements IServer {

	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private Server server;
	private ServletHandler handler;

	private int port = 0;

	/**
	 * Creates and initializes a new server with the given path, but does not start it.
	 * 
	 * @param port
	 */
	public HttpServer(int port) {
		this.port = port;
		server = new Server(port);
		handler = new ServletHandler();
		server.setHandler(handler);
	}

	/**
	 * Add a new servlet that handles server requests.
	 * 
	 * @param servlet handling requests to the given mappingPath
	 * @param mappingPath path that this servlet should serve, e.g. "/utils/getrandomint"
	 */
	protected void addServlet(Class<? extends Servlet> servlet, String mappingPath) {
		handler.addServletWithMapping(servlet, mappingPath);
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

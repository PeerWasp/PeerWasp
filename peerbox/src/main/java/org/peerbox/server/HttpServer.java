package org.peerbox.server;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer implements IServer {


	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
	
	
	private Server server; 
	private ServletHandler handler;
	
	private int port = 0;

	public HttpServer(int port) {
		server = new Server(port);
		handler = new ServletHandler();
	    server.setHandler(handler);
	}
	
	protected void addServlet(Class<? extends Servlet> servlet, String mappingPath) {
		 handler.addServletWithMapping(servlet, mappingPath);
	}
	
	@Override
	public void start() {
		try {
			server.start();
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

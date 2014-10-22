package org.peerbox.server.servlets;

import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.MimeTypes;
import org.peerbox.server.utils.PathDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class VersionsServlet extends HttpServlet  implements IServlet {
	
	private static final Logger logger = LoggerFactory.getLogger(VersionsServlet.class);
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	        throws ServletException, IOException {
		// check content type: json
		if(!req.getContentType().contains(MimeTypes.Type.APPLICATION_JSON.asString())) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, String.format(
						"Only JSON requests supported (MIME %s)",
						MimeTypes.Type.APPLICATION_JSON.asString()));
		}
		
		// read content
		char[] buffer = new char[req.getContentLength()];
		req.getReader().read(buffer);
		String content = new String(buffer);
		
		// deserialize into message
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Path.class, new PathDeserializer());
		Gson gson = gsonBuilder.create();

		VersionsMessage msg = null; 
		try {
			msg = gson.fromJson(content, VersionsMessage.class);
			logger.info("Got request for file versions: {}", msg.getPath());
			// todo: handle the message.
			
		} catch(JsonSyntaxException jsonEx) {
			logger.info("Could not parse message.");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not deserialize given input");
		}
		
		
	}
	
	private static class VersionsMessage {
		private Path path;	
		public Path getPath() {
			return path;
		}
	}
}

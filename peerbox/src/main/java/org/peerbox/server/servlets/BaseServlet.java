package org.peerbox.server.servlets;

import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.MimeTypes;
import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.servlets.messages.ServerReturnMessage;
import org.peerbox.server.utils.PathDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

abstract class BaseServlet<T> extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {

			if (!checkContentTypeJSON(req, resp)) {
				logger.error("Received request with wrong ContentType: {}", req.getContentType());
				sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.WRONG_CONTENT_TYPE));
				return;
			}

			String content = readContentAsString(req);
			if (content == null || content.isEmpty()) {
				sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.EMPTY_REQUEST));
				return;
			}

			T msg = deserializeMessage(content);
			if (msg == null) {
				throw new Exception("Message is null. Cannot interpret request message.");
			}

			handleRequest(msg);

			sendEmptyOK(resp);
		} catch (JsonSyntaxException e) {
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.DESERIALIZE_ERROR));
			logger.error("Could not deserialize json.", e);
		} catch (Exception e) {
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.REQUEST_EXCEPTION));
			logger.error("Unexpected exception. ", e);
		}
	}

	protected abstract void handleRequest(T msg) throws Exception;

	protected abstract T deserializeMessage(String jsonContent);

	protected Gson createGsonInstance() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Path.class, new PathDeserializer());
		return gsonBuilder.create();
	}

	protected String readContentAsString(HttpServletRequest req) throws IOException {
		// read content
		char[] buffer = new char[req.getContentLength()];
		req.getReader().read(buffer);
		return new String(buffer);
	}

	protected boolean checkContentTypeJSON(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// check content type: json
		if (req.getContentType() == null || !req.getContentType().contains(MimeTypes.Type.APPLICATION_JSON.asString())) {
			return false;
		}
		return true;
	}

	protected void sendErrorMessage(HttpServletResponse resp, ServerReturnMessage msg) throws IOException {
		resp.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		Gson gson = new Gson();
		String response = gson.toJson(msg);
		resp.getWriter().println(response);

		logger.error("Request failed: {} (Code {})", msg.getMessage(), msg.getCode());
	}

	protected void sendEmptyOK(HttpServletResponse resp) {
		resp.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}

package org.peerbox.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.servlets.messages.ServerReturnMessage;
import org.peerbox.server.servlets.messages.ShareMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Singleton;

@Singleton
public class ShareFolderServlet extends BaseServlet {

	private static final Logger logger = LoggerFactory.getLogger(ShareFolderServlet.class);
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		if (!checkContentTypeJSON(req, resp)) {
			logger.error("Received request with wrong ContentType: {}", req.getContentType());
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.WRONG_CONTENT_TYPE));
			return;
		}

		String content = readContentAsString(req);
		Gson gson = createGsonInstance();

		ShareMessage msg = null;
		try {
			msg = gson.fromJson(content, ShareMessage.class);
			shareFolder(msg);
		} catch (JsonSyntaxException jsonEx) {
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.DESERIALIZE_ERROR));
		}
	}

	private void shareFolder(ShareMessage msg) {
		logger.info("Got request to share folder: {}", msg.getPath());
	}

}

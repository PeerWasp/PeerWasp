package org.peerbox.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.peerbox.interfaces.IFileVersionHandler;
import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.servlets.messages.ServerReturnMessage;
import org.peerbox.server.servlets.messages.VersionsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class VersionsServlet extends BaseServlet {

	private static final Logger logger = LoggerFactory.getLogger(VersionsServlet.class);
	private static final long serialVersionUID = 1L;
	
	private final Provider<IFileVersionHandler> fileRecoveryHandlerProvider;
	
	@Inject
	public VersionsServlet(Provider<IFileVersionHandler> fileRecoveryHandlerProvider) {
		this.fileRecoveryHandlerProvider = fileRecoveryHandlerProvider;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!checkContentTypeJSON(req, resp)) {
			logger.error("Received request with wrong ContentType: {}", req.getContentType());
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.WRONG_CONTENT_TYPE));
			return;
		}
		
		String content = readContentAsString(req);
		Gson gson = createGsonInstance();

		VersionsMessage msg = null;
		try {
			msg = gson.fromJson(content, VersionsMessage.class);
			recover(msg);
		} catch (JsonSyntaxException jsonEx) {
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.DESERIALIZE_ERROR));
		}
		
	}

	private void recover(VersionsMessage msg) {
		logger.info("Got request for file versions: {}", msg.getPath());

		IFileVersionHandler handler = fileRecoveryHandlerProvider.get();
		handler.onFileVersionRequested(msg.getPath());
	}

}

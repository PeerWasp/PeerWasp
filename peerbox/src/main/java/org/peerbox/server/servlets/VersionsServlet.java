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

		try {
			
			if (!checkContentTypeJSON(req, resp)) {
				logger.error("Received request with wrong ContentType: {}", req.getContentType());
				sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.WRONG_CONTENT_TYPE));
				return;
			}
			
			String content = readContentAsString(req);
			if(content.isEmpty()) {
				sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.EMPTY_REQUEST));
				return;
			}
			
			Gson gson = createGsonInstance();
	
			VersionsMessage msg = null;
		
			msg = gson.fromJson(content, VersionsMessage.class);
			
			recover(msg);
			
			sendEmptyOK(resp);
			
		} catch (Exception e) {
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.DESERIALIZE_ERROR));
			return;
		}
	}


	private void recover(VersionsMessage msg) throws Exception {
		if(msg == null) {
			throw new IllegalArgumentException("msg==null");
		}
		if(msg.getPath() == null) {
			throw new Exception("msg.getPath()==null");
		}
		
		logger.info("Got request for file versions: {}", msg.getPath());
		IFileVersionHandler handler = fileRecoveryHandlerProvider.get();
		handler.onFileVersionRequested(msg.getPath());
	}

}

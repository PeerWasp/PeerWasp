package org.peerbox.server.servlets;

import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.peerbox.delete.FileDeleteHandler;
import org.peerbox.delete.IFileDeleteHandler;
import org.peerbox.server.servlets.messages.DeleteMessage;
import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.servlets.messages.ServerReturnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class DeleteServlet extends BaseServlet {

	private static final Logger logger = LoggerFactory.getLogger(DeleteServlet.class);
	private static final long serialVersionUID = 1L;
	
	private final Provider<IFileDeleteHandler> fileDeleteHandlerProvider;
	
	@Inject
	public DeleteServlet(Provider<IFileDeleteHandler> fileRecoveryDeleteProvider){
		this.fileDeleteHandlerProvider = fileRecoveryDeleteProvider;
	}

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

		DeleteMessage msg = null;
		try {
			msg = gson.fromJson(content, DeleteMessage.class);
			delete(msg);
		} catch (JsonSyntaxException jsonEx) {
			sendErrorMessage(resp, new ServerReturnMessage(ServerReturnCode.DESERIALIZE_ERROR));
		}
	}

	private void delete(DeleteMessage msg) {
		IFileDeleteHandler handler = fileDeleteHandlerProvider.get();
		for(Path path: msg.getPaths()){
			handler.deleteFile(path);
		}
	}

}

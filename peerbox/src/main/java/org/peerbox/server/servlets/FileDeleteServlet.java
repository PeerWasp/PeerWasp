package org.peerbox.server.servlets;

import java.nio.file.Path;

import org.peerbox.delete.IFileDeleteHandler;
import org.peerbox.server.servlets.messages.DeleteMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class FileDeleteServlet extends BaseServlet<DeleteMessage> {

	private static final Logger logger = LoggerFactory.getLogger(FileDeleteServlet.class);
	private static final long serialVersionUID = 1L;

	private final Provider<IFileDeleteHandler> fileDeleteHandlerProvider;

	@Inject
	public FileDeleteServlet(Provider<IFileDeleteHandler> fileRecoveryDeleteProvider) {
		super();
		this.fileDeleteHandlerProvider = fileRecoveryDeleteProvider;
	}

	@Override
	protected void handleRequest(DeleteMessage msg) throws Exception {
		if (msg.getPaths() == null) {
			throw new IllegalArgumentException("Paths is null. Cannot interpret request.");
		}

		IFileDeleteHandler handler = fileDeleteHandlerProvider.get();
		for (Path path : msg.getPaths()) {
			logger.info("Got request to delete file: '{}'", path);
			handler.deleteFile(path);
		}
	}

	@Override
	protected DeleteMessage deserializeMessage(final String jsonContent) {
		DeleteMessage msg = null;
		Gson gson = createGsonInstance();
		msg = gson.fromJson(jsonContent, DeleteMessage.class);
		return msg;
	}

}

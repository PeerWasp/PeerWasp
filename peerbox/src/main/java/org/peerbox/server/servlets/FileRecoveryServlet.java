package org.peerbox.server.servlets;

import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.server.servlets.messages.FileRecoveryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class FileRecoveryServlet extends BaseServlet<FileRecoveryMessage> {

	private static final Logger logger = LoggerFactory.getLogger(FileRecoveryServlet.class);
	private static final long serialVersionUID = 1L;

	private final Provider<IFileRecoveryHandler> fileRecoveryHandlerProvider;

	@Inject
	public FileRecoveryServlet(Provider<IFileRecoveryHandler> fileRecoveryHandlerProvider) {
		super();
		this.fileRecoveryHandlerProvider = fileRecoveryHandlerProvider;
	}

	@Override
	protected void handleRequest(FileRecoveryMessage msg) throws Exception {
		if (msg.getPath() == null) {
			throw new IllegalArgumentException("Path is null. Cannot interpret request.");
		}

		logger.info("Got request to recover file: '{}'", msg.getPath());
		IFileRecoveryHandler handler = fileRecoveryHandlerProvider.get();
		handler.recoverFile(msg.getPath());
	}

	@Override
	protected FileRecoveryMessage deserializeMessage(final String jsonContent) {
		FileRecoveryMessage msg = null;
		Gson gson = createGsonInstance();
		msg = gson.fromJson(jsonContent, FileRecoveryMessage.class);
		return msg;
	}

}

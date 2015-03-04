package org.peerbox.server.servlets;

import org.peerbox.server.servlets.messages.ShareMessage;
import org.peerbox.share.IShareFolderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ShareFolderServlet extends BaseServlet<ShareMessage> {

	private static final Logger logger = LoggerFactory.getLogger(ShareFolderServlet.class);
	private static final long serialVersionUID = 1L;

	private final Provider<IShareFolderHandler> shareFolderHandlerProvider;

	@Inject
	public ShareFolderServlet(Provider<IShareFolderHandler> shareFolderHandlerProvider) {
		super();
		this.shareFolderHandlerProvider = shareFolderHandlerProvider;
	}

	@Override
	protected void handleRequest(ShareMessage msg) throws Exception {
		if (msg.getPath() == null) {
			throw new IllegalArgumentException("Path is null. Cannot interpret request.");
		}

		logger.info("Got request to share folder: '{}'", msg.getPath());
		IShareFolderHandler handler = shareFolderHandlerProvider.get();
		handler.shareFolder(msg.getPath());
	}

	@Override
	protected ShareMessage deserializeMessage(final String jsonContent) {
		ShareMessage msg = null;
		Gson gson = createGsonInstance();
		msg = gson.fromJson(jsonContent, ShareMessage.class);
		return msg;
	}

}

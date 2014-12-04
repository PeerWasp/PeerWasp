package org.peerbox.guice.provider;

import org.peerbox.share.IShareFolderHandler;
import org.peerbox.share.ShareFolderHandler;

import com.google.inject.Provider;

public class ShareFolderHandlerProvider implements Provider<IShareFolderHandler> {

	@Override
	public IShareFolderHandler get() {
		return new ShareFolderHandler();
	}

}

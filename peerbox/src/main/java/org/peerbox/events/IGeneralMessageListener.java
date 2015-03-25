package org.peerbox.events;

import net.engio.mbassy.listener.Handler;

public interface IGeneralMessageListener extends IMessageListener {

	@Handler
	void onInformationMessage(InformationMessage message);

	@Handler
	void onWarningMessage(WarningMessage message);

}

package org.peerbox.app.manager.user;

import net.engio.mbassy.listener.Handler;

import org.peerbox.events.IMessageListener;

public interface IUserMessageListener extends IMessageListener {
	
	@Handler
	void onUserRegister(RegisterMessage register);
	
	@Handler
	void onUserLogin(LoginMessage login);

	@Handler
	void onUserLogout(LogoutMessage logout);
	
}

package org.peerbox.app.manager.user;

import org.peerbox.events.IMessageListener;

import net.engio.mbassy.listener.Handler;

public interface IUserMessageListener extends IMessageListener {
	
	@Handler
	void onUserRegister(RegisterMessage register);
	
	@Handler
	void onUserLogin(LoginMessage login);

	@Handler
	void onUserLogout(LogoutMessage logout);
	
}

package org.peerbox.app.manager.user;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.peerbox.ResultStatus;

public interface IUserManager {
	
	ResultStatus registerUser(final String username, final String password, final String pin) 
			throws NoPeerConnectionException;
	
	boolean isRegistered(final String userName) throws NoPeerConnectionException;
	
	ResultStatus loginUser(final String username, final String password, final String pin, final Path rootPath) 
			throws NoPeerConnectionException, IOException;
	
	boolean isLoggedIn() throws NoPeerConnectionException;
	
	ResultStatus logoutUser() 
			throws NoPeerConnectionException, NoSessionException;
}

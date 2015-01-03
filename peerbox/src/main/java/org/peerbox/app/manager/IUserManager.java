package org.peerbox.app.manager;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.ResultStatus;

public interface IUserManager {
	
	ResultStatus registerUser(final String username, final String password, final String pin) 
			throws NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException;
	
	boolean isRegistered(final String userName) throws NoPeerConnectionException;
	
	ResultStatus loginUser(final String username, final String password, final String pin, final Path rootPath) 
			throws NoPeerConnectionException, InvalidProcessStateException, ProcessExecutionException, IOException;
	
	boolean isLoggedIn() throws NoPeerConnectionException;
	
	ResultStatus logoutUser() 
			throws InvalidProcessStateException, ProcessExecutionException, NoPeerConnectionException, NoSessionException;
}

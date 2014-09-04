package org.peerbox.watchservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Calendar;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.io.DigestInputStream;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Action {
	
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;
	
	//TODO File path?
	//private File file = null;
	private String contentHash = "";
	private Path filePath;
	private ActionState currentState;
		
	public Action(){
		timestamp = Calendar.getInstance().getTimeInMillis(); 
		currentState = new InitialState();
		contentHash = new String("");
	}
	
	public Action(ActionState initialState, Path filePath){
		currentState = initialState;
		timestamp = Calendar.getInstance().getTimeInMillis();
		contentHash = computeContentHash(filePath);
		//file = getFileFromPath(filePath);
		this.filePath = filePath;
		
		
	}
	public Path getFilePath(){
		return filePath;//.toString();
	}
	private File getFileFromPath(Path filePath){
		if(filePath != null && filePath.toFile() != null){
			return filePath.toFile();
		}
		return null;
	}
	
	private String computeContentHash(Path filePath) {
		if(filePath != null && filePath.toFile() != null){
			try {
				byte[] rawHash = EncryptionUtil.generateMD5Hash(filePath.toFile());
				if(rawHash != null){
					return Action.createStringFromByteArray(rawHash);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Action.createStringFromByteArray(new byte[1]);
	}
	
	public static String createStringFromByteArray(byte[] bytes){
		String hashString = Base64.encode(bytes);
		return hashString;
	}
	
	public String getContentHash(){
		return contentHash;
	}
	
	public void setContentHash(String contentHash){
		this.contentHash = contentHash;
	}
	
	public void setTimeStamp(long timestamp) {
		// TODO Auto-generated method stub
		if(timestamp < this.timestamp){
			//this is clearly an error - but can it even occur?
		}
		this.timestamp = timestamp;
	}
	
	
	public void handleCreateEvent(){
		currentState = currentState.handleCreateEvent();
		contentHash = computeContentHash(filePath);
	}
	
	public void handleDeleteEvent(){
		currentState = currentState.handleDeleteEvent();
	}
	
	public void handleModifyEvent(){
		currentState = currentState.handleModifyEvent();
		contentHash = computeContentHash(filePath);
	}
	
	//execute action depending on state
	public void execute() throws NoSessionException, NoPeerConnectionException, IllegalFileLocation{
		logger.debug("Execute action...");
		// this may be async, i.e. do not wait on completion of the process
		// maybe return the IProcessComponent object such that the 
		// executor can be aware of the status (completion of task etc)

		currentState.execute(filePath);
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public ActionState getCurrentState(){	
			if (currentState.getClass() == CreateState.class){
				logger.debug("Current State: Create");
			} else if (currentState.getClass() == DeleteState.class){
				logger.debug("Current State: Delete");
			} else if (currentState.getClass() == ModifyState.class){
				logger.debug("Current State: Modify");
			} else if (currentState.getClass() == MoveState.class){
				logger.debug("Current State: Move");
			} else if (currentState.getClass() == InitialState.class){
				logger.debug("Current State: Initial");
			}
			
			return currentState;
		}
	
	public void setCurrentState(ActionState currentState){
		this.currentState = currentState;
	}
}

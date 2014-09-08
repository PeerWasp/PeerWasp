package org.peerbox.watchservice;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.EncryptionUtil;
import org.peerbox.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * The Action class provides a systematic and lose-coupled way to change the 
 * state of an object as part of the chosen state pattern design.
 * 
 * 
 * @author albrecht, anliker, winzenried
 *
 */

public class Action {
	
	private final static Logger logger = LoggerFactory.getLogger(Action.class);
	private long timestamp = Long.MAX_VALUE;
	
	private Path filePath;
	private String contentHash;
	private ActionState currentState;
	
	/**
	 * Initialize with timestamp and set currentState to initial state
	 */
	public Action(Path filePath){
		this.filePath = filePath;
		currentState = new InitialState(this);
		contentHash = computeContentHash(filePath);
		updateTimestamp();
	}
	
	
	
//	private File getFileFromPath(Path filePath){
//		if(filePath != null && filePath.toFile() != null){
//			return filePath.toFile();
//		}
//		return null;
//	}
	
	private void updateTimestamp() {
		timestamp = System.currentTimeMillis();
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
	
	/**
	 * changes the state of the currentState to Create state if current state allows it.
	 */
	public void handleCreateEvent(){
		currentState = currentState.handleCreateEvent();
		contentHash = computeContentHash(filePath);
		updateTimestamp();
	}
	
	/**
	 * changes the state of the currentState to Delete state if current state allows it.
	 */
	public void handleDeleteEvent(){
		currentState = currentState.handleDeleteEvent();
		updateTimestamp();
	}
	
	/**
	 * changes the state of the currentState to Modify state if current state allows it.
	 */
	public void handleModifyEvent(){
		currentState = currentState.handleModifyEvent();
		contentHash = computeContentHash(filePath);
		updateTimestamp();
	}
	
	public void handleMoveEvent(Path oldFilePath) {
		currentState = currentState.handleMoveEvent(oldFilePath);
		updateTimestamp();
	}

	/**
	 * Each state is able to execute an action as soon the state is considered as stable. 
	 * The action itself depends on the current state (e.g. add file, delete file, etc.)
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 * @throws IllegalFileLocation
	 */
	//execute action depending on state
	public void execute(FileManager fileManager) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation{
		logger.debug("Execute action...");
		// this may be async, i.e. do not wait on completion of the process
		// maybe return the IProcessComponent object such that the 
		// executor can be aware of the status (completion of task etc)

		currentState.execute(fileManager);
		currentState = new InitialState(this);
	}
	
	public Path getFilePath(){
		return filePath;//.toString();
	}

	public String getContentHash(){
		return contentHash;
	}

//	public void setContentHash(String contentHash){
//		this.contentHash = contentHash;
//	}

	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return current state object
	 */
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
	
//	public void setCurrentState(ActionState currentState){
//		this.currentState = currentState;
//	}
}

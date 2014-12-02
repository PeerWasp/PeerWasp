package org.peerbox.watchservice.states;

import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FolderComposite;
import org.peerbox.watchservice.IFileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;

public class LocalHardDeleteState extends AbstractActionState{
	private final static Logger logger = LoggerFactory.getLogger(LocalHardDeleteState.class);
	public LocalHardDeleteState(Action action) {
		super(action);
		// TODO Auto-generated constructor stub
	}
	public AbstractActionState getDefaultState(){
		logger.debug("Return to default state 'InitialState' as component was removed completely {}", action.getFilePath());
		return new InitialState(action);
	}
	
	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldPath) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleLocalCreate()");
	}
	
	public AbstractActionState handleLocalDelete(){
		logger.trace("File {}: entered handleLocalDelete", action.getFilePath());
		IFileEventManager eventManager = action.getEventManager();
		eventManager.getFileComponentQueue().remove(action.getFile());
//		if(action.getFile().isFile()){
//			String oldHash = action.getFile().getContentHash();
//			action.getFile().updateContentHash();
//			logger.debug("File: {}Previous content hash: {} new content hash: ", action.getFilePath(), oldHash, action.getFile().getContentHash());
//			SetMultimap<String, FileComponent> deletedFiles = action.getEventManager().getDeletedFileComponents();
//			deletedFiles.put(action.getFile().getContentHash(), action.getFile());
//			logger.debug("Put deleted file {} with hash {} to SetMultimap<String, FileComponent>", action.getFilePath(), action.getFile().getContentHash());
//		} else {
//
//			Map<String, FolderComposite> deletedFolders = eventManager.getDeletedByContentNamesHash();
//			logger.debug("Added folder {} with structure hash {} to deleted folders.", action.getFilePath(), action.getFile().getStructureHash());
//			deletedFolders.put(action.getFile().getStructureHash(), (FolderComposite)action.getFile());
//		}
		FileComponent comp = eventManager.getFileTree().deleteComponent(action.getFile().getPath().toString());
//		logger.debug("After delete hash of {} is {}", comp.getPath(), comp.getStructureHash());
//		eventManager.getFileComponentQueue().add(action.getFile());
		updateTimeAndQueue();
		return changeStateOnLocalDelete();
	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleLocalUpdate()");
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldFilePath) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleLocalMove()");
	}

	@Override
	public AbstractActionState handleLocalRecover(int version) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleLocalRecover()");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleRemoteCreate()");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleRemoteDelete()");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleRemoteUpdate()");
	}

	@Override
	public AbstractActionState handleRemoteMove(Path path) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("LocalHardDeleteState.handleRemoteMove()");
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException {
		Path path = action.getFilePath();
		logger.debug("Execute LOCAL DELETE: {}", path);
		IProcessComponent process = fileManager.delete(path.toFile());
		if(process != null){
			process.attachListener(new FileManagerProcessListener());
		}
	}

}

package org.peerbox.watchservice.states;

import java.nio.file.Path;

import org.apache.commons.lang3.NotImplementedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.peerbox.FileManager;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;

public class EstablishedState extends AbstractActionState{

	private static final Logger logger = LoggerFactory.getLogger(EstablishedState.class);
	
	public EstablishedState(Action action) {
		super(action);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractActionState changeStateOnLocalCreate() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public AbstractActionState changeStateOnLocalDelete() {
		return new LocalDeleteState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalUpdate() {
		// TODO Auto-generated method stub
		return new LocalUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnLocalMove(Path oldFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnLocalRecover(int versionToRecover) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteDelete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteCreate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractActionState changeStateOnRemoteUpdate() {
		// TODO Auto-generated method stub
		return new RemoteUpdateState(action);
	}

	@Override
	public AbstractActionState changeStateOnRemoteMove(Path oldFilePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(FileManager fileManager) throws NoSessionException,
			NoPeerConnectionException, IllegalFileLocation, InvalidProcessStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractActionState handleLocalCreate() {
		// TODO Auto-generated method stub
		return changeStateOnLocalCreate();
	}

//	@Override
//	public AbstractActionState handleLocalDelete() {
//		if(action.getFile().isFile()){
//			SetMultimap<String, FileComponent> deletedFiles = action.getFileEventManager().getDeletedFileComponents();
//			deletedFiles.put(action.getFile().getContentHash(), action.getFile());
//		}
//		IFileEventManager eventManager = action.getFileEventManager();
//		FileComponent file = eventManager.getFileTree().deleteComponent(action.getFilePath().toString());
//		eventManager.getFileComponentQueue().add(file);
//		return changeStateOnLocalDelete();
//	}

	@Override
	public AbstractActionState handleLocalUpdate() {
		// TODO Auto-generated method stub
		String newHash = PathUtils.computeFileContentHash(action.getFile().getPath());
		if(action.getFile().getContentHash().equals(newHash)){
			logger.info("The content hash has not changed despite the onLocalFileModified event. No actions taken & returned.");
			return this;
		}
		updateTimeAndQueue();
		return changeStateOnLocalUpdate();
	}

	@Override
	public AbstractActionState handleLocalMove(Path oldPath) {
		// TODO Auto-generated method stub
		throw new NotImplementedException("EstablishedState.handleLocalMove");
	}

	@Override
	public AbstractActionState handleLocalRecover() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("EstablishedState.handleLocalRecover");
	}

	@Override
	public AbstractActionState handleRemoteCreate() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("EstablishedState.handleRemoteCreate");
	}

	@Override
	public AbstractActionState handleRemoteDelete() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("EstablishedState.handleRemoteDelete");
	}

	@Override
	public AbstractActionState handleRemoteUpdate() {
		// TODO Auto-generated method stub
		updateTimeAndQueue();
		return changeStateOnRemoteUpdate();
	}

	@Override
	public AbstractActionState handleRemoteMove() {
		// TODO Auto-generated method stub
		throw new NotImplementedException("EstablishedState.handleRemoteMove");
	}

}

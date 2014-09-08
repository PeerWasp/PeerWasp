package org.peerbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.files.list.FileTaste;
import org.hive2hive.core.processes.implementations.files.recover.IVersionSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManager {
	
	private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

	public IProcessComponent add(File file) throws NoSessionException, NoPeerConnectionException,
			IllegalFileLocation {
		logger.debug("ADD - {}", file);
		return null;
	}

	public IProcessComponent update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException {
		logger.debug("UPDATE - {}", file);
		return null;
	}

	public IProcessComponent move(File source, File destination) throws NoSessionException,
			NoPeerConnectionException {
		logger.debug("MOVE - from: {}, to: {}", source, destination);
		return null;
	}

	public IProcessComponent delete(File file) throws NoSessionException, NoPeerConnectionException {
		logger.debug("DELETE - {}", file);
		return null;
	}

	public IProcessComponent recover(File file, IVersionSelector versionSelector)
			throws FileNotFoundException, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		logger.debug("RECOVER - {}", file);
		return null;
	}

	public IProcessComponent share(File folder, String userId, PermissionType permission)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		logger.debug("SHARE");
		return null;
	}

	public IResultProcessComponent<List<FileTaste>> getFileList() throws NoSessionException {
		return null;
	}
}

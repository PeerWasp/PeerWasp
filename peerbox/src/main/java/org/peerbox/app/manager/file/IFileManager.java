package org.peerbox.app.manager.file;

import java.io.File;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.h2h.ProcessHandle;

public interface IFileManager {

	ProcessHandle<Void> add(final File file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> update(final File file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> delete(final File file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> move(final File source, final File destination) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> download(final File file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> recover(final File file, final IVersionSelector versionSelector) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> share(final File folder, final String userId, final PermissionType permission) throws NoSessionException, NoPeerConnectionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException;

	ProcessHandle<FileNode> listFiles() throws NoSessionException, NoPeerConnectionException;

	boolean existsRemote(final Path path);

	boolean isSmallFile(final Path path);

	boolean isLargeFile(final Path path);

}
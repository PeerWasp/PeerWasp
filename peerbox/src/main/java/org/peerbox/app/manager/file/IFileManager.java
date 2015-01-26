package org.peerbox.app.manager.file;

import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.app.manager.ProcessHandle;

public interface IFileManager {

	ProcessHandle<Void> add(final Path file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> update(final Path file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> delete(final Path file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> move(final Path source, final Path destination) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> download(final Path file) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> recover(final Path file, final IVersionSelector versionSelector) throws NoSessionException, NoPeerConnectionException;

	ProcessHandle<Void> share(final Path folder, final String userId, final PermissionType permission) throws NoSessionException, NoPeerConnectionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException;

	ProcessHandle<FileNode> listFiles() throws NoSessionException, NoPeerConnectionException;

	boolean existsRemote(final Path path);

	boolean isSmallFile(final Path path);

	boolean isLargeFile(final Path path);

}
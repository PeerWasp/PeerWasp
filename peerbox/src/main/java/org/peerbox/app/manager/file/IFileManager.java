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

/**
 * The file manager is responsible for uploading and downloading files. It communicates with
 * the H2H network. Thus, the focus is on interaction with the network (and not local operations).
 *
 * @author albrecht
 *
 */
public interface IFileManager {

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createAddProcess(java.io.File)}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
	 * {@link ProcessHandle#executeAsync()}.
	 *
	 * @param file to add
	 * @return Process handle.
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	ProcessHandle<Void> add(final Path file) throws NoSessionException, NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createUpdateProcess(java.io.File)}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
     * {@link ProcessHandle#executeAsync()}.
     *
	 * @param file to updated
	 * @return Process handle
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	ProcessHandle<Void> update(final Path file) throws NoSessionException, NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createDeleteProcess(java.io.File)}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
	 * {@link ProcessHandle#executeAsync()}.
	 *
	 * @param file to delete
	 * @return process handle
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	ProcessHandle<Void> delete(final Path file) throws NoSessionException, NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createMoveProcess(java.io.File, java.io.File)}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
	 * {@link ProcessHandle#executeAsync()}.
	 *
	 * @param source of move
	 * @param destination of move
	 * @return process handle
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	ProcessHandle<Void> move(final Path source, final Path destination) throws NoSessionException, NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createDownloadProcess(java.io.File)}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
	 * {@link ProcessHandle#executeAsync()}.
	 *
	 * @param file to download
	 * @return process handle
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	ProcessHandle<Void> download(final Path file) throws NoSessionException, NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createRecoverProcess(java.io.File, IVersionSelector)}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
	 * {@link ProcessHandle#executeAsync()}.
	 *
	 * @param file to recover
	 * @param versionSelector selects version
	 * @return process handle
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	ProcessHandle<Void> recover(final Path file, final IVersionSelector versionSelector) throws NoSessionException, NoPeerConnectionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createShareProcess(java.io.File, String, PermissionType)}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
	 * {@link ProcessHandle#executeAsync()}.
	 *
	 * @param folder to share
	 * @param userId of other user
	 * @param permission to grant to other user
	 * @return process handle
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 * @throws IllegalArgumentException
	 * @throws InvalidProcessStateException
	 * @throws ProcessExecutionException
	 */
	ProcessHandle<Void> share(final Path folder, final String userId, final PermissionType permission) throws NoSessionException, NoPeerConnectionException, IllegalArgumentException, InvalidProcessStateException, ProcessExecutionException;

	/**
	 * See {@link org.hive2hive.core.api.interfaces.IFileManager#createFileListProcess()}
	 * The returned process is not not started yet. Call {@link ProcessHandle#execute()} or
	 * {@link ProcessHandle#executeAsync()}.
	 *
	 * @return process handle
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	ProcessHandle<FileNode> listFiles() throws NoSessionException, NoPeerConnectionException;

	/**
	 * Checks whether given file is available in the network (remote user profile).
	 *
	 * @param path to check
	 * @return true if file exists. False otherwise.
	 */
	boolean existsRemote(final Path path);

	/**
	 * Checks whether given file is a small file.
	 *
	 * @param path to check
	 * @return true if it is a small file. False otherwise.
	 */
	boolean isSmallFile(final Path path);

	/**
	 * Checks whether given file is a large file.
	 *
	 * @param path to check
	 * @return true if it is a large file. False otherwise.
	 */
	boolean isLargeFile(final Path path);

}
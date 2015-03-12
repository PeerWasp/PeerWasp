package org.peerbox.watchservice.conflicthandling;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.peerbox.app.manager.file.messages.LocalFileConflictMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

/**
 * This class is used to resolve file conflicts. The remote version
 * (downloaded from the network) of a file always wins, i.e. the local
 * version is renamed into a new file with the conflict-suffix.
 * @author Claudio
 *
 */
public class ConflictHandler {

	/**
	 * Generate the conflict name of a file with help of the
	 * current time and the conflict-suffix. Consider the example
	 * hello_world.txt
	 *
	 * @param path to be renamed
	 * @return The renamed version of the provided path. Example:
	 *         hello_world_CONFLICT_2015-03-02_18-03-17.txt
	 */
	public static Path rename(Path path) {
		String pathString = path.toString();
		String renamedFileString = null;
		String conflictSuffix = "_CONFLICT_";

		// get index of extension (even works with files like "hello.world.txt")
		int indexOfExtension = FilenameUtils.indexOfExtension(pathString);

		String fileName = pathString.substring(0, indexOfExtension);
		String fileExtension = pathString.substring(indexOfExtension);

		renamedFileString = fileName + conflictSuffix + currentDate() + fileExtension;
		Path renamedFile = Paths.get(renamedFileString);

		return renamedFile;
	}

	private static String currentDate() {
		// get current date and time and set it to specific format
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String currentDate = dateFormat.format(date);
		return currentDate;
	}

	/**
	 * Resolves a file conflict. To do that, the file is copied
	 * and renamed.
	 *
	 * @param file
	 */
	public static void resolveConflict(Path file) {
		resolveConflict(file, false);
	}

	/**
	 * Resolves a file conflict. To do that, the file is copied
	 * and renamed or only renamed.
	 *
	 * @param path of file to be renamed.
	 * @param moveFile true if the file should only be renamed and not copied.
	 */
	public static void resolveConflict(Path path, boolean moveFile) {
		resolveConflictAndNotifyGUI(path, moveFile, null);
	}

	/**
	 * @param path of file on which a conflict should be resolved.
	 * @param moveFile defines if the file is moved or copied before it is renamed.
	 * @param bus the message bus on which a message about the occurred conflict can
	 *            be transmitted.
	 */
	public static void resolveConflictAndNotifyGUI(Path path, boolean moveFile, MessageBus bus) {
		Path renamedFile = ConflictHandler.rename(path);
		try {
			if (moveFile) {
				Files.move(path, renamedFile);
			} else {
				Files.copy(path, renamedFile);
			}

			if (bus != null) {
				FileHelper fileHelper = new FileHelper(path, true);
				bus.publish(new LocalFileConflictMessage(fileHelper));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
